package io.jenkins.plugins.collector.model;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class JenkinsFilterParameter {

  private String jobName;
  private String beginTime;
  private String endTime;
  private String triggerBy;

}
