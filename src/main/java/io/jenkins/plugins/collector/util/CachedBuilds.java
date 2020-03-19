package io.jenkins.plugins.collector.util;

import hudson.model.Run;

import java.util.HashMap;

public class CachedBuilds {
  private HashMap<String, Run> failedJobMap = new HashMap<>();
  private HashMap<String, Run> lastBuildMap = new HashMap<>();

  public Run getFailedJobByJobName(String jobName) {
    return failedJobMap.get(jobName);
  }

  public void removeFailedJobByJobName(String jobName) {
    failedJobMap.remove(jobName);
  }

  public void putFailedJob(String jobName, Run run) {
    failedJobMap.put(jobName, run);
  }

  public void putLastBuild(String jobName, Run run) {
    lastBuildMap.put(jobName, run);
  }

  public Run getLastBuildByJobName(String jobName) {
    return lastBuildMap.get(jobName);
  }
}
