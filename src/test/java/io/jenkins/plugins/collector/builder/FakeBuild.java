package io.jenkins.plugins.collector.builder;

import hudson.model.Job;
import hudson.model.Result;
import hudson.model.Run;
import java.io.IOException;
import javax.annotation.Nonnull;

public class FakeBuild extends Run {

  public FakeBuild(Job project, Result result, long duration, FakeBuild previousBuild) throws IOException {
    super(project);
    this.result = result;
    this.duration = duration;
    this.previousBuild = previousBuild;
  }

  @Override
  public int compareTo(@Nonnull Run run) {
    return 0;
  }

  @Override
  public Result getResult() {
    return result;
  }

  @Override
  public boolean isBuilding() {
    return result == null;
  }
}
