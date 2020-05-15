package io.jenkins.plugins.collector.consumer.jenkins;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.google.inject.Inject;
import hudson.remoting.VirtualChannel;
import io.jenkins.plugins.collector.model.BuildInfo;
import io.jenkins.plugins.collector.model.BuildInfoResponse;
import io.jenkins.plugins.collector.model.JenkinsFilterParameter;
import java.io.File;
import java.io.IOException;
import java.util.List;
import jenkins.model.Jenkins;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JenkinsConsumer implements JenkinsMetrics {

  private static final Logger logger = LoggerFactory.getLogger(JenkinsConsumer.class);
  private Jenkins jenkins;
  private String folderPath;

  @Inject
  public JenkinsConsumer(Jenkins jenkins) {
    this.jenkins = jenkins;
    folderPath = jenkins.getRootDir().getPath() + "/cache4Opal/";
  }

  @Override
  public BuildInfoResponse getMetrics(JenkinsFilterParameter jenkinsFilterParameter) {
    return new JenkinsAdapter().adapt(getBuildInfoFromFile(jenkinsFilterParameter.getJobName()), jenkinsFilterParameter);
  }

  @Override
  public void accept(List<BuildInfo> buildInfos) {
    logger.info("start save the build info to local environment!");

    VirtualChannel channel = jenkins.getChannel();
    assert channel != null;

    buildInfos.forEach(buildInfo -> {
      try {
        Boolean result = channel.call(new CreateFileTask(folderPath, buildInfo.getJenkinsJob(), buildInfo.toString()));
        logger.info("The result of job {} is {}", buildInfo.getJenkinsJob(), result);
      } catch (InterruptedException | IOException e) {
        Thread.currentThread().interrupt();
        logger.error("failed to save the build info!");
      }
    });
  }

  private List<BuildInfo> getBuildInfoFromFile(String jobName) {
    File file = new File(folderPath + jobName);

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
