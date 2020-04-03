package io.jenkins.plugins.collector.builder;

import hudson.model.Result;
import hudson.model.Run;
import org.mockito.Mockito;

import static org.mockito.ArgumentMatchers.any;

public class MockBuildBuilder {

  Run previousBuild;
  Run nextBuild;
  Result result;
  long duration;
  long startTimeInMillis;

  public MockBuildBuilder previousBuild(Run previousBuild) {
    this.previousBuild = previousBuild;
    return this;
  }

  public MockBuildBuilder nextBuild(Run nextBuild) {
    this.nextBuild = nextBuild;
    return this;
  }

  public MockBuildBuilder result(Result result) {
    this.result = result;
    return this;
  }

  public MockBuildBuilder duration(long duration) {
    this.duration = duration;
    return this;
  }

  public MockBuildBuilder startTimeInMillis(long startTimeInMillis) {
    this.startTimeInMillis = startTimeInMillis;
    return this;
  }

  public MockBuild create() {
    MockBuild mockBuild = Mockito.mock(MockBuild.class);
    Mockito.when(mockBuild.getStartTimeInMillis()).thenReturn(startTimeInMillis);
    Mockito.when(mockBuild.getResult()).thenReturn(result);
    Mockito.when(mockBuild.getDuration()).thenReturn(duration);
    Mockito.when(mockBuild.getNextBuild()).thenReturn(nextBuild);
    Mockito.when(mockBuild.getPreviousBuild()).thenReturn(previousBuild);
    Mockito.when(mockBuild.createNextBuild(any(), any(), any())).thenCallRealMethod();
    Mockito.when(mockBuild.createPreviousBuild(any(), any(), any())).thenCallRealMethod();
    return mockBuild;
  }
}
