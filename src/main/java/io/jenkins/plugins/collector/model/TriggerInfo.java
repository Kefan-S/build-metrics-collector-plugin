package io.jenkins.plugins.collector.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
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
@JsonPropertyOrder({"lastCommitHash", "triggeredBy"})
public class TriggerInfo {

  @JsonIgnore
  private TriggerEnum triggerType;
  @JsonIgnore
  private List<ScmChangeInfo> scmChangeInfoList;
  private String triggeredBy;
  private String lastCommitHash;
}
