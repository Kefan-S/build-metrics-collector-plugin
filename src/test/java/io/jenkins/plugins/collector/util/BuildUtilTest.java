package io.jenkins.plugins.collector.util;

import hudson.model.Cause;
import hudson.model.Cause.UpstreamCause;
import hudson.model.FreeStyleBuild;
import hudson.model.Job;
import hudson.model.Result;
import hudson.model.Run;
import hudson.plugins.git.GitChangeSet;
import hudson.scm.ChangeLogSet;
import hudson.triggers.SCMTrigger.SCMTriggerCause;
import io.jenkins.plugins.collector.builder.MockBuild;
import io.jenkins.plugins.collector.builder.MockBuildBuilder;
import io.jenkins.plugins.collector.exception.InstanceMissingException;
import io.jenkins.plugins.collector.model.ScmChangeInfo;
import io.jenkins.plugins.collector.model.TriggerEnum;
import io.jenkins.plugins.collector.model.TriggerInfo;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import jenkins.model.Jenkins;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Jenkins.class, BuildUtil.class})
public class BuildUtilTest {

  @Test
  public void should_be_calculated_when_check_success_build_given_a_first_success_build() {
    MockBuild lastBuild = new MockBuildBuilder().startTimeInMillis(0).duration(20).result(Result.SUCCESS).previousBuild(null).create();

    assertTrue(BuildUtil.isFirstSuccessfulBuildAfterError(lastBuild));
  }

  @Test
  public void should_be_calculated_when_check_success_build_given_a_last_success_build_after_failed_build() {
    MockBuild previousBuild = new MockBuildBuilder().startTimeInMillis(0).duration(20).result(Result.FAILURE).previousBuild(null).create();
    MockBuild lastBuild = previousBuild.createNextBuild(30L, 50L, Result.SUCCESS);

    assertTrue(BuildUtil.isFirstSuccessfulBuildAfterError(lastBuild));
  }

  @Test
  public void should_be_calculated_when_check_success_build_given_a_last_success_build_after_success_build() {
    MockBuild previousBuild = new MockBuildBuilder().startTimeInMillis(0).duration(20).result(Result.SUCCESS).previousBuild(null).create();
    MockBuild lastBuild = previousBuild.createNextBuild(30L, 50L, Result.SUCCESS);

    assertTrue(BuildUtil.isFirstSuccessfulBuildAfterError(lastBuild));
  }

  @Test
  public void should_be_calculated_when_check_success_build_given_a_last_success_build_before_a_running_build() {
    MockBuild previousBuild = new MockBuildBuilder().startTimeInMillis(0).duration(20).result(Result.SUCCESS).previousBuild(null).create();
    previousBuild.createNextBuild(10L, 100L, null);

    assertTrue(BuildUtil.isFirstSuccessfulBuildAfterError(previousBuild));
  }

  @Test
  public void should_calculated_given_a_previous_success_build_completed_after_failed_build() {
    MockBuild previousBuild = new MockBuildBuilder().startTimeInMillis(0).duration(100).result(Result.SUCCESS).previousBuild(null).create();
    previousBuild.createNextBuild(10L, 20L, Result.FAILURE);

    assertTrue(BuildUtil.isFirstSuccessfulBuildAfterError(previousBuild));
  }

  @Test
  public void should_not_be_calculated_given_a_previous_success_build_completed_after_success_build() {
    MockBuild previousBuild = new MockBuildBuilder().startTimeInMillis(0).duration(100).result(Result.SUCCESS).previousBuild(null).create();
    previousBuild.createNextBuild(10L, 20L, Result.SUCCESS);

    assertFalse(BuildUtil.isFirstSuccessfulBuildAfterError(previousBuild));
  }

  @Test
  public void should_not_be_calculated_given_a_last_success_build_before_a_running_build() {
    MockBuild previousBuild = new MockBuildBuilder().startTimeInMillis(0).duration(20).result(Result.FAILURE).previousBuild(null).create();
    previousBuild.createNextBuild(10L, 100L, null);

    assertFalse(BuildUtil.isFirstSuccessfulBuildAfterError(previousBuild));
  }

  @Test
  public void should_not_be_calculated_given_a_latest_failure_build() {
    MockBuild previousBuild = new MockBuildBuilder().startTimeInMillis(0).duration(20).result(Result.FAILURE).previousBuild(null).create();

    assertFalse(BuildUtil.isFirstSuccessfulBuildAfterError(previousBuild));
  }

  @Test
  public void should_not_be_calculated_given_a_latest_failure_build_before_a_no_overtime_successful_build() {
    MockBuild previousBuild = new MockBuildBuilder().startTimeInMillis(0).duration(20).result(Result.FAILURE).previousBuild(null).create();
    previousBuild.createNextBuild(30L, 10L, Result.SUCCESS);

    assertFalse(BuildUtil.isFirstSuccessfulBuildAfterError(previousBuild));
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
  public void should_get_jobName_when_get_jobName_given_an_successful_build() {
    Run fakeRun = Mockito.mock(Run.class, Answers.RETURNS_DEEP_STUBS);
    when(fakeRun.getParent().getFullName()).thenReturn("name");

    String jobName = BuildUtil.getJobName(fakeRun);

    assertEquals("name", jobName);
  }

  @Test
  public void should_get_jobName_when_get_jobName_given_an_running_build() {
    Run fakeRun = Mockito.mock(Run.class, Mockito.RETURNS_DEEP_STUBS);
    when(fakeRun.getParent().getFullName()).thenReturn("name");

    String jobName = BuildUtil.getJobName(fakeRun);

    assertEquals("name", jobName);
  }

  @Test
  public void should_get_success_result_when_get_result_value_given_an_successful_build() {
    Run fakeRun = Mockito.mock(Run.class, Answers.RETURNS_DEEP_STUBS);
    when(fakeRun.getResult()).thenReturn(Result.SUCCESS);

    String resultValue = BuildUtil.getResultValue(fakeRun);

    assertEquals("0", resultValue);
  }

  @Test
  public void should_get_no_result_value_when_get_jobName_given_an_running_build() {
    Run fakeRun = Mockito.mock(Run.class, Mockito.RETURNS_DEEP_STUBS);
    when(fakeRun.getResult()).thenReturn(null);

    String resultValue = BuildUtil.getResultValue(fakeRun);

    assertEquals("-1", resultValue);
  }

  @Test
  public void should_get_scm_change_set_correctly_when_get_scm_Change_Info_given_work_flow_run_with_change_log() {
    WorkflowRun run = Mockito.mock(WorkflowRun.class);
    ChangeLogSet<GitChangeSet> changeLogSet = mock(ChangeLogSet.class);
    GitChangeSet gitChangeSet = mock(GitChangeSet.class);

    when(run.getChangeSets()).thenReturn(Arrays.asList(changeLogSet));
    when(changeLogSet.getItems()).thenReturn(new GitChangeSet[]{gitChangeSet});
    when(gitChangeSet.getAuthorName()).thenReturn("github-user");
    when(gitChangeSet.getComment()).thenReturn("commit message");
    when(gitChangeSet.getTimestamp()).thenReturn(1588218559L);
    when(gitChangeSet.getCommitId()).thenReturn("commit-hash");

    List<ScmChangeInfo> result = BuildUtil.getScmChangeInfo(run);

    assertEquals(1, result.size());
    assertEquals("github-user", result.get(0).getUserId());
    assertEquals("commit message", result.get(0).getCommitMessage());
    assertEquals(1588218559L, result.get(0).getCommitTimeStamp());
    assertEquals("commit-hash", result.get(0).getCommitHash());
  }

  @Test
  public void should_get_scm_change_set_correctly_when_get_scm_Change_Info_given_work_flow_run_with_empty_change_log() {
    WorkflowRun run = Mockito.mock(WorkflowRun.class);

    when(run.getChangeSets()).thenReturn(Collections.emptyList());

    assertEquals(0, BuildUtil.getScmChangeInfo(run).size());

  }

  @Test
  public void should_get_scm_change_set_correctly_when_get_scm_Change_Info_given_free_style_build_with_change_log() {
    FreeStyleBuild run = Mockito.mock(FreeStyleBuild.class);
    ChangeLogSet<GitChangeSet> changeLogSet = mock(ChangeLogSet.class);
    GitChangeSet gitChangeSet = mock(GitChangeSet.class);

    when(run.getChangeSets()).thenReturn(Arrays.asList(changeLogSet));
    when(changeLogSet.getItems()).thenReturn(new GitChangeSet[]{gitChangeSet});
    when(gitChangeSet.getAuthorName()).thenReturn("github-user");
    when(gitChangeSet.getComment()).thenReturn("commit message");
    when(gitChangeSet.getTimestamp()).thenReturn(1588218559L);
    when(gitChangeSet.getCommitId()).thenReturn("commit-hash");

    List<ScmChangeInfo> result = BuildUtil.getScmChangeInfo(run);

    assertEquals(1, result.size());
    assertEquals("github-user", result.get(0).getUserId());
    assertEquals("commit message", result.get(0).getCommitMessage());
    assertEquals(1588218559L, result.get(0).getCommitTimeStamp());
    assertEquals("commit-hash", result.get(0).getCommitHash());
  }

  @Test
  public void should_get_scm_change_set_correctly_when_get_scm_Change_Info_given_free_style_build_with_empty_change_log() {
    FreeStyleBuild run = Mockito.mock(FreeStyleBuild.class);

    when(run.getChangeSets()).thenReturn(Collections.emptyList());

    assertEquals(0, BuildUtil.getScmChangeInfo(run).size());

  }

  @Test
  public void should_return_null_when_get_scm_Change_Info_given_neither_work_flow_run_nor_free_style_build() {
    Run run = Mockito.mock(Run.class);

    assertNull(BuildUtil.getScmChangeInfo(run));
  }

  @Test
  public void should_return_trigger_info_correctly_when_get_trigger_info() {
    FreeStyleBuild run = Mockito.mock(FreeStyleBuild.class, RETURNS_DEEP_STUBS);
    when(run.getCause(UpstreamCause.class)).thenReturn(null);
    when(run.getCauses().get(0)).thenReturn(mock(SCMTriggerCause.class));
    ChangeLogSet<GitChangeSet> changeLogSet = mock(ChangeLogSet.class);
    GitChangeSet gitChangeSet1 = mock(GitChangeSet.class);
    GitChangeSet gitChangeSet2 = mock(GitChangeSet.class);

    when(run.getChangeSets()).thenReturn(Arrays.asList(changeLogSet));
    when(changeLogSet.getItems()).thenReturn(new GitChangeSet[]{gitChangeSet1, gitChangeSet2});
    when(gitChangeSet1.getAuthorName()).thenReturn("github-user1");
    when(gitChangeSet1.getComment()).thenReturn("commit message1");
    when(gitChangeSet1.getTimestamp()).thenReturn(1588218558L);
    when(gitChangeSet1.getCommitId()).thenReturn("commit-hash1");
    when(gitChangeSet2.getAuthorName()).thenReturn("github-user2");
    when(gitChangeSet2.getComment()).thenReturn("commit message2");
    when(gitChangeSet2.getTimestamp()).thenReturn(1588218559L);
    when(gitChangeSet2.getCommitId()).thenReturn("commit-hash2");

    TriggerInfo result = BuildUtil.getTriggerInfo(run);

    assertEquals(TriggerEnum.SCM_TRIGGER, result.getTriggerType());
    assertEquals("github-user2", result.getTriggeredBy());
  }

  @Test
  public void should_return_original_cause_when_get_original_cause_given_build_triggered_by_upstream_cause() {
    Run fakeUpstreamRun = Mockito.mock(Run.class, RETURNS_DEEP_STUBS);
    Run fakeRun = Mockito.mock(Run.class);
    mockStatic(Jenkins.class);
    Job fakeJob = Mockito.mock(Job.class);
    Jenkins jenkins = Mockito.mock(Jenkins.class);
    UpstreamCause upstreamCause = Mockito.mock(UpstreamCause.class);
    Cause mockCause = mock(Cause.class);

    when(fakeRun.getCause(UpstreamCause.class)).thenReturn(upstreamCause);
    when(Jenkins.getInstanceOrNull()).thenReturn(jenkins);
    when(jenkins.getItemByFullName(any(), any())).thenReturn(fakeJob);
    when(fakeJob.getBuildByNumber(anyInt())).thenReturn(fakeUpstreamRun);
    when(fakeUpstreamRun.getCauses().get(0)).thenReturn(mockCause);
    when(fakeUpstreamRun.getCause(UpstreamCause.class)).thenReturn(null);

    assertEquals(mockCause, BuildUtil.getOriginalCause(fakeRun));
  }

  @Test
  public void should_return_original_cause_when_get_original_cause_given_build_triggered_directly() {
    Run run = Mockito.mock(Run.class, RETURNS_DEEP_STUBS);
    Cause mockCause = mock(Cause.class);
    when(run.getCause(UpstreamCause.class)).thenReturn(null);
    when(run.getCauses().get(0)).thenReturn(mockCause);

    assertEquals(mockCause, BuildUtil.getOriginalCause(run));
  }

  @Test(expected = InstanceMissingException.class)
  public void should_throw_Jenkins_Instance_MissingException_when_get_original_cause_given_job_absent() {
    Run fakeRun = Mockito.mock(Run.class);
    mockStatic(Jenkins.class);
    Jenkins jenkins = Mockito.mock(Jenkins.class);
    UpstreamCause upstreamCause = Mockito.mock(UpstreamCause.class);

    when(fakeRun.getCause(UpstreamCause.class)).thenReturn(upstreamCause);
    when(Jenkins.getInstanceOrNull()).thenReturn(jenkins);
    BuildUtil.getOriginalCause(fakeRun);
  }

  @Test(expected = InstanceMissingException.class)
  public void should_throw_Jenkins_Instance_MissingException_when_get_original_cause_given_jenkins_instance_absent() {
    Run fakeRun = Mockito.mock(Run.class);
    mockStatic(Jenkins.class);
    UpstreamCause upstreamCause = Mockito.mock(UpstreamCause.class);

    when(fakeRun.getCause(UpstreamCause.class)).thenReturn(upstreamCause);
    BuildUtil.getOriginalCause(fakeRun);
  }
}
