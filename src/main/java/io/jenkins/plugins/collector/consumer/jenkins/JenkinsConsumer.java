package io.jenkins.plugins.collector.consumer.jenkins;

import hudson.FilePath;
import hudson.remoting.VirtualChannel;
import io.jenkins.plugins.collector.model.BuildInfo;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.function.Consumer;
import jenkins.model.Jenkins;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JenkinsConsumer implements Consumer<List<BuildInfo>> {

  private static final Logger logger = LoggerFactory.getLogger(JenkinsConsumer.class);
  private Jenkins jenkins;
  private String folderPath;

  public JenkinsConsumer(Jenkins jenkins) {
    this.jenkins = jenkins;
    folderPath = jenkins.getRootDir().getPath() + "/cache4Opal/";
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

  public InputStream getDataStream(String jobName) {
    FilePath textFile = new FilePath(new File(folderPath+jobName));

    try {
      if (textFile.exists()) {
        return textFile.read();
      } else {
        logger.warn("No data for this Job!");
        return null;
      }
    } catch (IOException | InterruptedException e) {
      Thread.currentThread().interrupt();
      logger.error("failed to get data!");
      return null;
    }
  }
}
