package io.jenkins.plugins.collector.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class SCMChangeInfo {

  private String userId;
  private String commitHash;
  private String commitMessage;
  private long commitTimeStamp;

}
