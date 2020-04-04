package io.jenkins.plugins.collector.util;

import hudson.model.Cause;
import hudson.model.Result;
import hudson.model.Run;
import hudson.triggers.SCMTrigger;
import io.jenkins.plugins.collector.builder.MockBuild;
import io.jenkins.plugins.collector.builder.MockBuildBuilder;
import org.junit.Test;
import org.mockito.Answers;
import org.mockito.Mockito;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

public class BuildUtilTest {

  @Test
  public void should_be_calculated_when_check_success_build_given_a_first_success_build() {
    MockBuild lastBuild = new MockBuildBuilder().startTimeInMillis(0).duration(20).result(Result.SUCCESS).previousBuild(null).create();

    assertTrue(BuildUtil.isFirstSuccessfulBuildAfterError(lastBuild.getNextBuild(), lastBuild));
  }

  @Test
  public void should_be_calculated_when_check_success_build_given_a_last_success_build_after_failed_build() {
    MockBuild previousBuild = new MockBuildBuilder().startTimeInMillis(0).duration(20).result(Result.FAILURE).previousBuild(null).create();
    MockBuild lastBuild = previousBuild.createNextBuild(30L, 50L, Result.SUCCESS);

    assertTrue(BuildUtil.isFirstSuccessfulBuildAfterError(lastBuild.getNextBuild(), lastBuild));
  }

  @Test
  public void should_be_calculated_when_check_success_build_given_a_last_success_build_after_success_build() {
    MockBuild previousBuild = new MockBuildBuilder().startTimeInMillis(0).duration(20).result(Result.SUCCESS).previousBuild(null).create();
    MockBuild lastBuild = previousBuild.createNextBuild(30L, 50L, Result.SUCCESS);

    assertTrue(BuildUtil.isFirstSuccessfulBuildAfterError(lastBuild.getNextBuild(), lastBuild));
  }

  @Test
  public void should_be_calculated_when_check_success_build_given_a_last_success_build_before_a_running_build() {
    MockBuild previousBuild = new MockBuildBuilder().startTimeInMillis(0).duration(20).result(Result.SUCCESS).previousBuild(null).create();
    MockBuild lastBuild = previousBuild.createNextBuild(10L, 100L, null);

    assertTrue(BuildUtil.isFirstSuccessfulBuildAfterError(lastBuild, previousBuild));
  }

  @Test
  public void should_not_be_calculated_given_a_previous_success_build_completed_after_failed_build() {
    MockBuild previousBuild = new MockBuildBuilder().startTimeInMillis(0).duration(100).result(Result.SUCCESS).previousBuild(null).create();
    MockBuild lastBuild = previousBuild.createNextBuild(10L, 20L, Result.FAILURE);

    assertFalse(BuildUtil.isFirstSuccessfulBuildAfterError(lastBuild, previousBuild));
  }

  @Test
  public void should_not_be_calculated_given_a_previous_success_build_completed_after_success_build() {
    MockBuild previousBuild = new MockBuildBuilder().startTimeInMillis(0).duration(100).result(Result.SUCCESS).previousBuild(null).create();
    MockBuild lastBuild = previousBuild.createNextBuild(10L, 20L, Result.SUCCESS);

    assertFalse(BuildUtil.isFirstSuccessfulBuildAfterError(lastBuild, previousBuild));
  }

  @Test
  public void is_complete_over_time_given_previous_build_is_running_after_next_build() {
    MockBuild previousBuild = new MockBuildBuilder().startTimeInMillis(0).duration(100).result(null).previousBuild(null).create();
    MockBuild lastBuild = previousBuild.createNextBuild(10L, 20L, Result.SUCCESS);

    assertTrue(BuildUtil.isCompleteOvertime(previousBuild, lastBuild));
  }

  @Test
  public void is_complete_over_time_given_previous_build_complete_after_next_build() {
    MockBuild previousBuild = new MockBuildBuilder().startTimeInMillis(0).duration(100).result(Result.SUCCESS).previousBuild(null).create();
    MockBuild lastBuild = previousBuild.createNextBuild(10L, 20L, Result.SUCCESS);

    assertTrue(BuildUtil.isCompleteOvertime(previousBuild, lastBuild));
  }

  @Test
  public void is_not_complete_over_time_given_previous_build_complete_before_next_build() {
    MockBuild previousBuild = new MockBuildBuilder().startTimeInMillis(0).duration(20).result(Result.FAILURE).previousBuild(null).create();
    MockBuild lastBuild = previousBuild.createNextBuild(30L, 50L, Result.SUCCESS);

    assertFalse(BuildUtil.isCompleteOvertime(previousBuild, lastBuild));
  }

  @Test
  public void should_return_max_value_when_calculate_end_time_given_a_running_build() {
    MockBuild build = new MockBuildBuilder().startTimeInMillis(0).duration(20).result(null).previousBuild(null).create();
    assertEquals(Long.MAX_VALUE, BuildUtil.getBuildEndTime(build));
  }

  @Test
  public void should_return_build_end_time_when_calculate_end_time_given_a_completed_build() {
    MockBuild build = new MockBuildBuilder().startTimeInMillis(0).duration(20).result(Result.SUCCESS).previousBuild(null).create();
    assertEquals(20, BuildUtil.getBuildEndTime(build));
  }

  @Test
  public void should_get_labels_when_get_labels_given_an_successful_build() {
    Run fakeRun = Mockito.mock(Run.class, Answers.RETURNS_DEEP_STUBS);
    when(fakeRun.getParent().getFullName()).thenReturn("name");
    when(fakeRun.getResult()).thenReturn(Result.SUCCESS);
    when(fakeRun.getCause(Cause.UpstreamCause.class)).thenReturn(null);
    when(fakeRun.getCause(SCMTrigger.SCMTriggerCause.class)).thenReturn(new SCMTrigger.SCMTriggerCause("something"));

    String[] labels = BuildUtil.getLabels(fakeRun);

    assertArrayEquals(new String[]{"name", "SCM", "SUCCESS"}, labels);
  }

  @Test
  public void should_get_labels_when_get_labels_given_an_running_build() {
    Run fakeRun = Mockito.mock(Run.class, Mockito.RETURNS_DEEP_STUBS);
    when(fakeRun.getParent().getFullName()).thenReturn("name");
    when(fakeRun.getResult()).thenReturn(null);
    when(fakeRun.getCause(Cause.UpstreamCause.class)).thenReturn(null);
    when(fakeRun.getCause(SCMTrigger.SCMTriggerCause.class)).thenReturn(new SCMTrigger.SCMTriggerCause("something"));

    String[] labels = BuildUtil.getLabels(fakeRun);

    assertArrayEquals(new String[]{"name", "SCM", "RUNNING"}, labels);
  }

  @Test
  public void should_return_user_id_when_get_trigger_given_user_triggered_build() {
    Run fakeRun = Mockito.mock(Run.class);

    Cause.UserIdCause userIdCause = new Cause.UserIdCause("user-id");
    when(fakeRun.getCause(Cause.UpstreamCause.class)).thenReturn(null);
    when(fakeRun.getCause(SCMTrigger.SCMTriggerCause.class)).thenReturn(null);
    when(fakeRun.getCause(Cause.UserIdCause.class)).thenReturn(userIdCause);

    String trigger = BuildUtil.getTrigger(fakeRun);
    assertEquals("user-id", trigger);
  }

  @Test
  public void should_return_unKnown_user_when_get_trigger_given_anonymous_user_triggered_build() {
    Run fakeRun = Mockito.mock(Run.class);

    Cause.UserIdCause userIdCause = new Cause.UserIdCause(null);
    when(fakeRun.getCause(Cause.UpstreamCause.class)).thenReturn(null);
    when(fakeRun.getCause(SCMTrigger.SCMTriggerCause.class)).thenReturn(null);
    when(fakeRun.getCause(Cause.UserIdCause.class)).thenReturn(userIdCause);

    String trigger = BuildUtil.getTrigger(fakeRun);
    assertEquals("UnKnown User", trigger);
  }

  @Test
  public void should_return_unKnown_when_get_trigger_given_neither_scm_nor_user_triggered_build() {
    Run fakeRun = Mockito.mock(Run.class);

    when(fakeRun.getCause(Cause.UpstreamCause.class)).thenReturn(null);
    when(fakeRun.getCause(SCMTrigger.SCMTriggerCause.class)).thenReturn(null);
    when(fakeRun.getCause(Cause.UserIdCause.class)).thenReturn(null);

    String trigger = BuildUtil.getTrigger(fakeRun);
    assertEquals("UnKnown", trigger);
  }
}