package io.jenkins.plugins.collector.model;

import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class TriggerInfo {

  private TriggerEnum triggerType;
  private List<SCMChangeInfo> scmChangeInfoList;
  private String jenkinsUserName;
}
