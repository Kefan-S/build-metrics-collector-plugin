package io.jenkins.plugins.collector.exceptions;

public class JenkinsInstanceMissingException extends RuntimeException {
    public JenkinsInstanceMissingException() {
        super("Can not get Jenkins instance.");
    }
}
