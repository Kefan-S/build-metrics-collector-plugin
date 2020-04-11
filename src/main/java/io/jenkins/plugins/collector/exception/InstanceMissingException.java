package io.jenkins.plugins.collector.exception;

public class InstanceMissingException extends RuntimeException {

  public InstanceMissingException() {
    super("Can not get Jenkins or Job instance.");
  }
}
