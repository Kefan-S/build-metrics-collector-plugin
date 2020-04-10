package io.jenkins.plugins.collector.util;

import hudson.model.Cause;
import hudson.model.Cause.UpstreamCause;
import hudson.model.Job;
import hudson.model.Result;
import hudson.model.Run;
import hudson.triggers.SCMTrigger;
import io.jenkins.plugins.collector.builder.MockBuild;
import io.jenkins.plugins.collector.builder.MockBuildBuilder;
import io.jenkins.plugins.collector.exception.InstanceMissingException;
import jenkins.model.Jenkins;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Jenkins.class, BuildUtil.class})
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
  public void should_not_be_calculated_given_a_last_success_build_before_a_running_build() {
    MockBuild previousBuild = new MockBuildBuilder().startTimeInMillis(0).duration(20).result(Result.FAILURE).previousBuild(null).create();
    MockBuild lastBuild = previousBuild.createNextBuild(10L, 100L, null);

    assertFalse(BuildUtil.isFirstSuccessfulBuildAfterError(lastBuild, previousBuild));
  }

  @Test
  public void should_not_be_calculated_given_a_latest_failure_build() {
    MockBuild previousBuild = new MockBuildBuilder().startTimeInMillis(0).duration(20).result(Result.FAILURE).previousBuild(null).create();

    assertFalse(BuildUtil.isFirstSuccessfulBuildAfterError(previousBuild.getNextBuild(), previousBuild));
  }

  @Test
  public void should_not_be_calculated_given_a_latest_failure_build_before_a_no_overtime_successful_build() {
    MockBuild previousBuild = new MockBuildBuilder().startTimeInMillis(0).duration(20).result(Result.FAILURE).previousBuild(null).create();
    previousBuild.createNextBuild(30L, 10L, Result.SUCCESS);

    assertFalse(BuildUtil.isFirstSuccessfulBuildAfterError(previousBuild.getNextBuild(), previousBuild));
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
    when(fakeRun.getCause(Cause.UpstreamCause.class)).thenReturn(null);
    when(fakeRun.getCause(SCMTrigger.SCMTriggerCause.class)).thenReturn(new SCMTrigger.SCMTriggerCause("something"));

    String[] labels = BuildUtil.getLabels(fakeRun);

    assertArrayEquals(new String[]{"name", "SCM", "RUNNING"}, labels);
  }

  @Test
  public void should_return_user_id_when_get_trigger_given_user_triggered_build() {
    Run fakeRun = Mockito.mock(Run.class);

    Cause.UserIdCause userIdCause = new Cause.UserIdCause("user-id");
    when(fakeRun.getCause(Cause.UserIdCause.class)).thenReturn(userIdCause);

    String trigger = BuildUtil.getTrigger(fakeRun);
    assertEquals("user-id", trigger);
  }

  @Test
  public void should_return_unKnown_user_when_get_trigger_given_anonymous_user_triggered_build() {
    Run fakeRun = Mockito.mock(Run.class);

    Cause.UserIdCause userIdCause = new Cause.UserIdCause(null);
    when(fakeRun.getCause(Cause.UserIdCause.class)).thenReturn(userIdCause);

    String trigger = BuildUtil.getTrigger(fakeRun);
    assertEquals("UnKnown User", trigger);
  }

  @Test
  public void should_return_unKnown_when_get_trigger_given_neither_scm_nor_user_triggered_build() {
    Run fakeRun = Mockito.mock(Run.class);

    String trigger = BuildUtil.getTrigger(fakeRun);
    assertEquals("UnKnown", trigger);
  }

  @Test
  public void should_return_scm_when_get_trigger_given_scm_triggered_upstream_build() {
    Run fakeUpstreamRun = Mockito.mock(Run.class);
    Run fakeRun = Mockito.mock(Run.class);
    mockStatic(Jenkins.class);
    Job fakeJob = Mockito.mock(Job.class);
    Jenkins jenkins = Mockito.mock(Jenkins.class);
    UpstreamCause upstreamCause = Mockito.mock(UpstreamCause.class);

    when(fakeRun.getCause(UpstreamCause.class)).thenReturn(upstreamCause);
    when(Jenkins.getInstanceOrNull()).thenReturn(jenkins);
    when(jenkins.getItemByFullName(any(), any())).thenReturn(fakeJob);
    when(fakeJob.getBuildByNumber(anyInt())).thenReturn(fakeUpstreamRun);
    when(fakeUpstreamRun.getCause(SCMTrigger.SCMTriggerCause.class)).thenReturn(new SCMTrigger.SCMTriggerCause("something"));
    assertEquals("SCM", BuildUtil.getTrigger(fakeRun));
  }

  @Test
  public void should_return_user_id_when_get_trigger_given_user_triggered_upstream_build() {
    Run fakeUpstreamRun = Mockito.mock(Run.class);
    Run fakeRun = Mockito.mock(Run.class);
    mockStatic(Jenkins.class);
    Job fakeJob = Mockito.mock(Job.class);
    Jenkins jenkins = Mockito.mock(Jenkins.class);
    UpstreamCause upstreamCause = Mockito.mock(UpstreamCause.class);

    when(fakeRun.getCause(UpstreamCause.class)).thenReturn(upstreamCause);
    when(Jenkins.getInstanceOrNull()).thenReturn(jenkins);
    when(jenkins.getItemByFullName(any(), any())).thenReturn(fakeJob);
    when(fakeJob.getBuildByNumber(anyInt())).thenReturn(fakeUpstreamRun);
    when(fakeUpstreamRun.getCause(Cause.UserIdCause.class)).thenReturn(new Cause.UserIdCause("user-id"));

    assertEquals("user-id", BuildUtil.getTrigger(fakeRun));
  }

  @Test
  public void should_return_UnKnown_User_when_get_trigger_given_anonymous_user_triggered_upstream_build() {
    Run fakeUpstreamRun = Mockito.mock(Run.class);
    Run fakeRun = Mockito.mock(Run.class);
    mockStatic(Jenkins.class);
    Job fakeJob = Mockito.mock(Job.class);
    Jenkins jenkins = Mockito.mock(Jenkins.class);
    UpstreamCause upstreamCause = Mockito.mock(UpstreamCause.class);

    when(fakeRun.getCause(UpstreamCause.class)).thenReturn(upstreamCause);
    when(Jenkins.getInstanceOrNull()).thenReturn(jenkins);
    when(jenkins.getItemByFullName(any(), any())).thenReturn(fakeJob);
    when(fakeJob.getBuildByNumber(anyInt())).thenReturn(fakeUpstreamRun);
    when(fakeUpstreamRun.getCause(Cause.UserIdCause.class)).thenReturn(new Cause.UserIdCause(null));

    assertEquals("UnKnown User", BuildUtil.getTrigger(fakeRun));
  }

  @Test
  public void should_return_UnKnown_when_get_trigger_given_neither_scm_nor_user_triggered_upstream_build() {
    Run fakeUpstreamRun = Mockito.mock(Run.class);
    Run fakeRun = Mockito.mock(Run.class);
    mockStatic(Jenkins.class);
    Job fakeJob = Mockito.mock(Job.class);
    Jenkins jenkins = Mockito.mock(Jenkins.class);
    UpstreamCause upstreamCause = Mockito.mock(UpstreamCause.class);

    when(fakeRun.getCause(UpstreamCause.class)).thenReturn(upstreamCause);
    when(Jenkins.getInstanceOrNull()).thenReturn(jenkins);
    when(jenkins.getItemByFullName(any(), any())).thenReturn(fakeJob);
    when(fakeJob.getBuildByNumber(anyInt())).thenReturn(fakeUpstreamRun);

    assertEquals("UnKnown", BuildUtil.getTrigger(fakeRun));
  }

  @Test(expected = InstanceMissingException.class)
  public void should_throw_Jenkins_Instance_MissingException_when_get_trigger_given_job_absent() {
    Run fakeRun = Mockito.mock(Run.class);
    mockStatic(Jenkins.class);
    Jenkins jenkins = Mockito.mock(Jenkins.class);
    UpstreamCause upstreamCause = Mockito.mock(UpstreamCause.class);

    when(fakeRun.getCause(UpstreamCause.class)).thenReturn(upstreamCause);
    when(Jenkins.getInstanceOrNull()).thenReturn(jenkins);
    BuildUtil.getTrigger(fakeRun);
  }

  @Test(expected = InstanceMissingException.class)
  public void should_throw_Jenkins_Instance_MissingException_when_get_trigger_given_jenkins_instance_absent() {
    Run fakeRun = Mockito.mock(Run.class);
    mockStatic(Jenkins.class);
    UpstreamCause upstreamCause = Mockito.mock(UpstreamCause.class);

    when(fakeRun.getCause(UpstreamCause.class)).thenReturn(upstreamCause);
    BuildUtil.getTrigger(fakeRun);
  }
}
