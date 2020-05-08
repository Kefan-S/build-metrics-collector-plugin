package io.jenkins.plugins.collector.consumer.jenkins;

import hudson.FilePath;
import hudson.remoting.Callable;
import hudson.remoting.VirtualChannel;
import io.jenkins.plugins.collector.model.BuildInfo;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.function.Consumer;
import jenkins.model.Jenkins;
import jenkins.security.Roles;
import org.jenkinsci.remoting.RoleChecker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JenkinsStorageConsumer implements Consumer<List<BuildInfo>> {

  private static final Logger logger = LoggerFactory.getLogger(JenkinsStorageConsumer.class);
  private Jenkins jenkins;

  public JenkinsStorageConsumer(Jenkins jenkins) {
    this.jenkins = jenkins;
  }

  @Override
  public void accept(List<BuildInfo> buildInfos) {
    logger.info("start save the build info to local environment!");

    VirtualChannel channel = jenkins.getChannel();
    String folderPath = jenkins.getRootDir().getPath() + "/cache4Opal/";

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

  public static class CreateFileTask implements Serializable, Callable<Boolean, IOException> {

    private static final long serialVersionUID = 1L;
    private final String fileContent;
    private final String fileName;
    private final String folderPath;

    CreateFileTask(String folderPath, String fileName, String fileContent) {
      this.folderPath = folderPath;
      this.fileName = fileName;
      this.fileContent = fileContent;
    }

    @Override
    public void checkRoles(RoleChecker roleChecker) throws SecurityException {
      roleChecker.check(this, Roles.SLAVE);
    }

    @Override
    public Boolean call() throws IOException {
      try {
        FilePath folder = new FilePath(new File(folderPath));
        FilePath textFile = new FilePath(new File(folderPath+fileName));
        String finalFileContent = "";
        String eol = System.getProperty("line.separator");

        if (!textFile.exists()) {
          finalFileContent = fileContent;
        } else {
          String existingFileContents = textFile.readToString();
          finalFileContent = existingFileContents.concat(eol + fileContent);
        }
        textFile.deleteContents();
        finalFileContent = finalFileContent.replaceAll("\n", System.lineSeparator());
        folder.mkdirs();
        textFile.write(finalFileContent, "UTF-8");
      } catch (Exception e) {
        logger.error(e.getMessage());
        return false;
      }
      return true;
    }
  }
}
