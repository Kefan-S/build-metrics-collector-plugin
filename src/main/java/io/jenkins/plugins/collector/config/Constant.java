package io.jenkins.plugins.collector.config;

import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableList;

public class Constant {
  public static final String METRICS_NAME_PREFIX = "builds";
  public static final String METRICS_NAMESPACE = "default";
  public static final String METRICS_SUBSYSTEM = "jenkins";
  public static final List<String> METRICS_LABEL_NAME_ARRAY = unmodifiableList(asList("jenkinsJob", "triggeredBy", "result"));
  public static final String BUILD_NO_RESULT_STATUS_VALUE = "-1";
}
