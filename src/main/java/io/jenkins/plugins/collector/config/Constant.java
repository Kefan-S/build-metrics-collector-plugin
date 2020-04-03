package io.jenkins.plugins.collector.config;

public class Constant {

  public static final String METRICS_NAME_PREFIX = "builds";
  public static final String METRICS_NAMESPACE = "default";
  public static final String METRICS_SUBSYSTEM = "jenkins";
  public static final String[] METRICS_LABEL_NAME_ARRAY = {"jenkins_job", "triggeredBy", "result"};
  public static final String BUILD_NO_RESULT_STATUS = "RUNNING";
}
