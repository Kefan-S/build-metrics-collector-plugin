package io.jenkins.plugins.collector.model;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TriggerInfo {

  private TriggerEnum triggerType;
  private List<SCMChangeInfo> scmChangeInfoList;
  private String jenkinsUserName;
}
