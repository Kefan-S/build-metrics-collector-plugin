package io.jenkins.plugins.collector.consumer.jenkins;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.google.inject.Inject;
import hudson.model.Item;
import hudson.model.TopLevelItem;
import hudson.remoting.VirtualChannel;
import io.jenkins.plugins.collector.model.BuildInfo;
import io.jenkins.plugins.collector.model.BuildInfoResponse;
import io.jenkins.plugins.collector.model.JenkinsFilterParameter;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import jenkins.model.Jenkins;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JenkinsConsumer implements JenkinsMetrics {

  private static final Logger logger = LoggerFactory.getLogger(JenkinsConsumer.class);
  private Jenkins jenkins;

  @Inject
  public JenkinsConsumer(Jenkins jenkins) {
    this.jenkins = jenkins;
  }

  @Override
  public BuildInfoResponse getMetrics(JenkinsFilterParameter jenkinsFilterParameter) {
    return new JenkinsAdapter().adapt(getBuildInfoFromFile(jenkinsFilterParameter.getJobName()), jenkinsFilterParameter);
  }

  @Override
  public List<String> getBuildUsers(String jobName) {
    List<BuildInfo> buildInfos = getBuildInfoFromFile(jobName);
    return Objects.requireNonNull(buildInfos).stream()
        .map(buildInfo -> buildInfo.getTriggerInfo().getTriggeredBy())
        .collect(Collectors.toList());
  }

  @Override
  public void accept(List<BuildInfo> buildInfos) {
    logger.info("start save the build info to local environment!");

    VirtualChannel channel = jenkins.getChannel();
    assert channel != null;

    buildInfos.forEach(buildInfo -> {
      try {
        TopLevelItem item = jenkins.getItem(buildInfo.getJenkinsJob());
        if (Objects.nonNull(item)) {
          File rootFile = item.getRootDir();
          Boolean result = channel.call(new CreateFileTask(rootFile.getPath(), buildInfo.toString()));
          logger.info("The result of job {} is {}", buildInfo.getJenkinsJob(), result);
        }
      } catch (Exception e) {
        Thread.currentThread().interrupt();
        logger.error("failed to save the build info!");
      }
    });
  }

  private List<BuildInfo> getBuildInfoFromFile(String jobName) {

    String folderPath = Optional.ofNullable(jenkins.getItem(jobName))
        .map(Item::getRootDir).map(File::getPath).orElse("");
    File file = new File(folderPath + "/opal");
    try {
      if (file.exists()) {
        MappingIterator<BuildInfo> personIter = new CsvMapper().readerWithTypedSchemaFor(BuildInfo.class).readValues(file);
        return personIter.readAll();
      } else {
        logger.warn("No data for this Job!");
        return null;
      }
    } catch (IOException e) {
      Thread.currentThread().interrupt();
      logger.error("failed to get data!");
      return null;
    }
  }
}
