package io.jenkins.plugins.collector.config;

public interface Constant {
    String METRICS_NAME_PREFIX = "builds";
    String METRICS_NAMESPACE = "default";
    String METRICS_SUBSYSTEM = "jenkins";
    String[] METRICS_LABEL_NAME_ARRAY = {"jenkins_job"};
}
