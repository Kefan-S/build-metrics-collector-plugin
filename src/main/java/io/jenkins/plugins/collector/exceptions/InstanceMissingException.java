package io.jenkins.plugins.collector.exceptions;

public class InstanceMissingException extends RuntimeException {

  public InstanceMissingException() {
    super("Can not get Jenkins or Job instance.");
  }
}
