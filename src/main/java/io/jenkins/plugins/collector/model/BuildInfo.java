package io.jenkins.plugins.collector.model;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonPropertyOrder({"startTime", "duration", "leadTime", "recoverTime", "jenkinsJob", "job", "result", "TriggerInfo"})
public class BuildInfo {

  private Long startTime;
  private Long duration;
  private Long leadTime;
  private Long recoverTime;
  private String jenkinsJob;
  private String job;
  private String result;
  @JsonUnwrapped
  private TriggerInfo triggerInfo;

  @Override
  public String toString() {
    return startTime + "," + duration + "," + leadTime + "," + recoverTime + "," + jenkinsJob + "," + job + "," + result + "," + triggerInfo.getTriggeredBy();
  }
}
