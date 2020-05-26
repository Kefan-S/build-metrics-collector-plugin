package io.jenkins.plugins.collector.consumer.jenkins;

import hudson.FilePath;
import hudson.remoting.Callable;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import jenkins.security.Roles;
import lombok.EqualsAndHashCode;
import org.jenkinsci.remoting.RoleChecker;

@EqualsAndHashCode
public class CreateFileTask implements Serializable, Callable<Boolean, IOException> {

  private static final long serialVersionUID = 1L;
  private static final String fileName = "/opal";
  private final String fileContent;
  private final String folderPath;

  CreateFileTask(String folderPath, String fileContent) {
    this.folderPath = folderPath;
    this.fileContent = fileContent;
  }

  @Override
  public void checkRoles(RoleChecker roleChecker) throws SecurityException {
    roleChecker.check(this, Roles.SLAVE);
  }

  @Override
  public Boolean call() throws IOException {
    try {
      FilePath textFile = new FilePath(new File(folderPath + fileName));
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
      textFile.write(finalFileContent, "UTF-8");
    } catch (Exception e) {
      throw new IOException(e.getMessage(), e);
    }
    return true;
  }
}
