package io.jenkins.plugins.collector.builder;

import hudson.model.Job;
import hudson.model.Run;
import hudson.model.TopLevelItem;
import hudson.model.TopLevelItemDescriptor;
import java.util.SortedMap;
import org.junit.Rule;
import org.jvnet.hudson.test.JenkinsRule;

public class FakeJob extends Job implements TopLevelItem {

  @Rule
  public static JenkinsRule rule = new JenkinsRule();

  private int i;
  private final SortedMap<Integer, FakeBuild> runs;

  public FakeJob(SortedMap<Integer, FakeBuild> runs) {
    super(rule.jenkins, "name");
    this.runs = runs;
    i = 1;
  }

  @Override
  public int assignBuildNumber() {
    return i++;
  }

  @Override
  public SortedMap<Integer, ? extends Run> _getRuns() {
    return runs;
  }

  @Override
  public boolean isBuildable() {
    return true;
  }

  @Override
  protected void removeRun(Run run) {
  }

  public TopLevelItemDescriptor getDescriptor() {
    throw new AssertionError();
  }

  public static Job createMockProject(final SortedMap<Integer, FakeBuild> runs) {
    return new FakeJob(runs);
  }
}
