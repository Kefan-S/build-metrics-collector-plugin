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

public class JenkinsStorageConsumer implements Consumer<List<BuildInfo>> {

  private static final Logger logger = LoggerFactory.getLogger(JenkinsStorageConsumer.class);
  private Jenkins jenkins;
  private String folderPath;

  public JenkinsStorageConsumer(Jenkins jenkins) {
    this.jenkins = jenkins;
    folderPath = jenkins.getRootDir().getPath() + "/cache4Opal/";
  }

  @Override
  public void accept(List<BuildInfo> buildInfos) {
    logger.info("start save the build info to local environment!");

    VirtualChannel channel = jenkins.getChannel();

    for (BuildInfo buildInfo : buildInfos) {
      boolean result = false;
      String fileName = buildInfo.getJenkinsJob();
      try {
        String content = buildInfo.toString();
        assert channel != null;
        result = channel.call(new CreateFileTask(folderPath, fileName, content));
      } catch (InterruptedException | IOException e) {
        Thread.currentThread().interrupt();
        logger.error("failed to save the build info!");
      }

      logger.info(String.format("The result of job %s is %s", buildInfo.getJenkinsJob(), result));
    }
    logger.info("end save the build info to local environment!");
  }

  public InputStream getDataStream(String jobName) {
    FilePath textFile = new FilePath(new File(folderPath+jobName));

    try {
      if (textFile.exists()) {
        return textFile.read();
      } else {
        logger.warn(String.format("No data for Job %s!", jobName));
        return null;
      }
    } catch (IOException | InterruptedException e) {
       Thread.currentThread().interrupt();
       logger.error("failed to get data!");
       return null;
    }
  }
}
