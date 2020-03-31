package io.jenkins.plugins.collector.exceptions;

public class NoJenkinsException extends RuntimeException {
    public NoJenkinsException() {
        super("Can not get Jenkins instance.");
    }
}
