package io.jenkins.plugins.collector.exception;

public class NoSuchBuildException extends RuntimeException {

  public NoSuchBuildException(String message) {
    super(message);
  }
}
