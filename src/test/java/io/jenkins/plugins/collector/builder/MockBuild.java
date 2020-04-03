package io.jenkins.plugins.collector.builder;

import hudson.model.Job;
import hudson.model.Result;
import hudson.model.Run;
import java.io.IOException;
import javax.annotation.Nonnull;
import org.mockito.Mockito;

public class MockBuild extends Run {

  protected MockBuild(@Nonnull Job job) throws IOException {
    super(job);
  }

  MockBuild createPreviousBuild(long interval, long duration, Result result) {
    if (interval <= 0 || duration <= 0 || (getStartTimeInMillis() - interval <= 0)) {
      throw new IllegalArgumentException();
    }
    MockBuild previousBuild = new MockBuildBuilder()
        .startTimeInMillis(getStartTimeInMillis() - interval)
        .duration(duration)
        .nextBuild(this)
        .result(result)
        .create();
    Mockito.when(this.getPreviousBuild()).thenReturn(previousBuild);
    return previousBuild;
  }

  MockBuild createNextBuild(long interval, long duration, Result result) {
    if (interval <= 0 || duration <= 0) {
      throw new IllegalArgumentException();
    }
    MockBuild nextBuild = new MockBuildBuilder()
        .startTimeInMillis(getStartTimeInMillis() + interval)
        .duration(duration)
        .previousBuild(this)
        .result(result)
        .create();
    Mockito.when(this.getNextBuild()).thenReturn(nextBuild);
    return nextBuild;
  }

}
