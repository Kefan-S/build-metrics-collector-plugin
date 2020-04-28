package io.jenkins.plugins.collector.handler;

import hudson.model.Result;
import hudson.model.Run;
import io.jenkins.plugins.collector.builder.MockBuild;
import io.jenkins.plugins.collector.builder.MockBuildBuilder;
import io.jenkins.plugins.collector.util.BuildUtil;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static io.jenkins.plugins.collector.handler.LeadTimeHandlerTest.LEADTIME_HANDLER_LABELS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({BuildUtil.class, RecoverTimeNewHandler.class})
public class RecoverTimeNewHandlerTest {

  private RecoverTimeNewHandler recoverTimeHandler;

  @Before
  public void setUp() {
    PowerMockito.mockStatic(BuildUtil.class);
    recoverTimeHandler = new RecoverTimeNewHandler();
    when(BuildUtil.isCompleteOvertime(any(), any())).thenCallRealMethod();
    when(BuildUtil.getBuildEndTime(any())).thenCallRealMethod();
    PowerMockito.when(BuildUtil.isFirstSuccessfulBuildAfterError(any())).thenCallRealMethod();
    when(BuildUtil.getLabels(any())).thenReturn(LEADTIME_HANDLER_LABELS);
    when(BuildUtil.isSuccessfulBuild(any())).thenCallRealMethod();
    when(BuildUtil.isAbortBuild(any())).thenCallRealMethod();
  }

  @Test
  public void should_do_nothing_if_current_build_is_not_first_successful_build_after_error() throws Exception {
    RecoverTimeNewHandler recoverTimeHandler = PowerMockito.spy(new RecoverTimeNewHandler());
    Run currentBuild = new MockBuildBuilder().create();

    PowerMockito.when(BuildUtil.isFirstSuccessfulBuildAfterError(currentBuild)).thenReturn(false);
    PowerMockito.doReturn(1L).when(recoverTimeHandler, "calculateRecoverTime",currentBuild);

    Long actual = recoverTimeHandler.apply(currentBuild);

    assertNull(actual);
    PowerMockito.verifyPrivate(recoverTimeHandler, never()).invoke("calculateRecoverTime",currentBuild);
  }

  @Test
  public void should_calculate_recover_time_if_current_build_is_first_successful_build_after_error() throws Exception {
    RecoverTimeNewHandler recoverTimeHandler = PowerMockito.spy(new RecoverTimeNewHandler());
    Run currentBuild = new MockBuildBuilder().create();

    PowerMockito.when(BuildUtil.isFirstSuccessfulBuildAfterError(currentBuild)).thenReturn(true);
    PowerMockito.doReturn(1L).when(recoverTimeHandler, "calculateRecoverTime", currentBuild);

    Long actual = recoverTimeHandler.apply(currentBuild);

    assertEquals(1L, actual.longValue());
    PowerMockito.verifyPrivate(recoverTimeHandler, times(1)).invoke("calculateRecoverTime", currentBuild);
  }

  @Test
  public void should_return_different_metric_data_when_handle_different_build_given_different_build() throws Exception {
    RecoverTimeNewHandler recoverTimeHandler = PowerMockito.spy(new RecoverTimeNewHandler());
    Run currentBuild1 = new MockBuildBuilder().create();
    Run currentBuild2 = new MockBuildBuilder().create();

    PowerMockito.when(BuildUtil.isFirstSuccessfulBuildAfterError(currentBuild1)).thenReturn(true);
    PowerMockito.doReturn(1L).when(recoverTimeHandler, "calculateRecoverTime", currentBuild1);
    PowerMockito.when(BuildUtil.isFirstSuccessfulBuildAfterError(currentBuild2)).thenReturn(true);
    PowerMockito.doReturn(2L).when(recoverTimeHandler, "calculateRecoverTime", currentBuild2);

    Long actual1 = recoverTimeHandler.apply(currentBuild1);
    Long actual2 = recoverTimeHandler.apply(currentBuild2);

    assertEquals(1L, actual1.longValue());
    assertEquals(2L, actual2.longValue());
  }

  @Test
  public void should_not_set_value_to_metrics_if_calculated_lead_time_is_negative() throws Exception {
    RecoverTimeNewHandler recoverTimeHandler = PowerMockito.spy(new RecoverTimeNewHandler());
    Run currentBuild = new MockBuildBuilder().create();

    PowerMockito.when(BuildUtil.isFirstSuccessfulBuildAfterError(currentBuild)).thenReturn(true);
    PowerMockito.doReturn(-1L).when(recoverTimeHandler, "calculateRecoverTime", currentBuild);

    Long actual = recoverTimeHandler.apply(currentBuild);

    PowerMockito.verifyPrivate(recoverTimeHandler, times(1)).invoke("calculateRecoverTime", currentBuild);
    assertNull(actual);
  }

  @Test
  public void should_do_nothing_when_call_accept_given_a_unsuccessful_build() throws Exception {
    RecoverTimeNewHandler recoverTimeHandler = PowerMockito.spy(new RecoverTimeNewHandler());
    Run currentBuild = new MockBuildBuilder().result(Result.FAILURE).create();

    Long actual = recoverTimeHandler.apply(currentBuild);

    PowerMockito.verifyPrivate(recoverTimeHandler, never()).invoke("calculateRecoverTime", currentBuild);
    assertNull(actual);
  }

  @Test
  public void should_return_recovery_time_given_a_success_build_after_a_failed_build() {
    MockBuild previousBuild = new MockBuildBuilder().startTimeInMillis(70).duration(20).result(Result.FAILURE).previousBuild(null).create();
    MockBuild lastBuild = previousBuild.createNextBuild(30L, 50L, Result.SUCCESS);

    Long actual = recoverTimeHandler.apply(lastBuild);

    assertEquals(60L, actual.longValue());
  }

  @Test
  public void should_recovery_first_failed_build_given_a_success_build_after_two_failed_build() {
    MockBuild previousPreviousBuild = new MockBuildBuilder().startTimeInMillis(70).duration(20).result(Result.FAILURE).previousBuild(null).create();
    MockBuild previousBuild = previousPreviousBuild.createNextBuild(30L, 50L, Result.FAILURE);
    MockBuild lastBuild = previousBuild.createNextBuild(30L, 50L, Result.SUCCESS);

    Long actual = recoverTimeHandler.apply(lastBuild);

    Long exceptedRecoveryTime = BuildUtil.getBuildEndTime(lastBuild) - BuildUtil.getBuildEndTime(previousPreviousBuild);
    assertEquals(exceptedRecoveryTime, actual);
  }

  @Test
  public void should_recovery_first_failed_build_given_a_later_success_build_completed_first_after_failed_build() {
    MockBuild previousPreviousBuild = new MockBuildBuilder().startTimeInMillis(70).duration(20).result(Result.FAILURE).previousBuild(null).create();
    MockBuild previousBuild = previousPreviousBuild.createNextBuild(30L, 150L, Result.SUCCESS);
    MockBuild lastBuild = previousBuild.createNextBuild(30L, 50L, Result.SUCCESS);

    Long actual = recoverTimeHandler.apply(lastBuild);

    Long exceptedRecoveryTime = BuildUtil.getBuildEndTime(lastBuild) - BuildUtil.getBuildEndTime(previousPreviousBuild);
    assertEquals(exceptedRecoveryTime, actual);

  }

  @Test
  public void should_not_send_recoveryTime_given_a_success_build_after_a_success_build() {
    MockBuild previousBuild = new MockBuildBuilder().startTimeInMillis(70).duration(20).result(Result.SUCCESS).previousBuild(null).create();
    MockBuild lastBuild = previousBuild.createNextBuild(30L, 50L, Result.SUCCESS);

    Long actual = recoverTimeHandler.apply(lastBuild);

    assertNull(actual);
  }

  @Test
  public void should_not_send_recoveryTime_given_previous_build_is_completed_after_current_build() {
    MockBuild previousBuild = new MockBuildBuilder().startTimeInMillis(70).duration(100).result(Result.SUCCESS).previousBuild(null).create();
    MockBuild lastBuild = previousBuild.createNextBuild(10L, 50L, Result.SUCCESS);

    Long actual = recoverTimeHandler.apply(lastBuild);

    assertNull(actual);
  }

  @Test
  public void should_not_send_recoveryTime_given_a_failed_build() {
    MockBuild lastBuild = new MockBuildBuilder().startTimeInMillis(70).duration(100).result(Result.FAILURE).previousBuild(null).create();

    Long actual = recoverTimeHandler.apply(lastBuild);

    assertNull(actual);
  }

  @Test
  public void should_not_send_recoveryTime_given_a_first_success_build() {
    MockBuild lastBuild = new MockBuildBuilder().startTimeInMillis(70).duration(100).result(Result.SUCCESS).previousBuild(null).create();

    Long actual = recoverTimeHandler.apply(lastBuild);

    assertNull(actual);
  }

}
