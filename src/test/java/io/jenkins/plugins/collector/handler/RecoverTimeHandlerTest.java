package io.jenkins.plugins.collector.handler;

import hudson.model.Result;
import hudson.model.Run;
import io.jenkins.plugins.collector.builder.MockBuild;
import io.jenkins.plugins.collector.builder.MockBuildBuilder;
import io.jenkins.plugins.collector.util.BuildUtil;
import io.prometheus.client.Gauge;
import io.prometheus.client.Gauge.Child;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static io.jenkins.plugins.collector.config.Constant.METRICS_LABEL_NAME_ARRAY;
import static io.jenkins.plugins.collector.handler.LeadTimeHandlerTest.LEADTIME_HANDLER_LABELS;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({BuildUtil.class, RecoverTimeHandler.class})
public class RecoverTimeHandlerTest {

  private RecoverTimeHandler recoverTimeHandler;

  private Gauge mockRecoverTimeMetrics;
  private Child mockRecoveryTimeChild;

  private String[] labels = (String[]) METRICS_LABEL_NAME_ARRAY.toArray();

  @Before
  public void setUp() {
    PowerMockito.mockStatic(BuildUtil.class);
    mockRecoveryTimeChild = Mockito.mock(Child.class);
    mockRecoverTimeMetrics = Mockito.mock(Gauge.class);
    recoverTimeHandler = new RecoverTimeHandler(mockRecoverTimeMetrics);
    when(BuildUtil.isCompleteOvertime(any(), any())).thenCallRealMethod();
    when(BuildUtil.getBuildEndTime(any())).thenCallRealMethod();
    PowerMockito.when(BuildUtil.isFirstSuccessfulBuildAfterError(any())).thenCallRealMethod();
    when(BuildUtil.getLabels(any())).thenReturn(LEADTIME_HANDLER_LABELS);
    when(BuildUtil.isSuccessfulBuild(any())).thenCallRealMethod();
    when(BuildUtil.isAbortBuild(any())).thenCallRealMethod();
  }

  @Test
  public void should_do_nothing_if_current_build_is_not_first_successful_build_after_error() throws Exception {
    RecoverTimeHandler recoverTimeHandler = PowerMockito.spy(new RecoverTimeHandler(mockRecoverTimeMetrics));
    Run currentBuild = new MockBuildBuilder().create();
    Run previousBuild = currentBuild.getPreviousBuild();

    doReturn(mockRecoveryTimeChild).when(mockRecoverTimeMetrics).labels(labels);
    PowerMockito.when(BuildUtil.isFirstSuccessfulBuildAfterError(currentBuild)).thenReturn(false);
    PowerMockito.doReturn(1L).when(recoverTimeHandler, "calculateRecoverTime", previousBuild, currentBuild);

    recoverTimeHandler.accept(currentBuild);

    PowerMockito.verifyPrivate(recoverTimeHandler, never()).invoke("calculateRecoverTime", previousBuild, currentBuild);
    verify(mockRecoverTimeMetrics, never()).labels(labels);
    verify(mockRecoveryTimeChild, never()).set(1L);

  }

  @Test
  public void should_calculate_recover_time_if_current_build_is_first_successful_build_after_error() throws Exception {
    RecoverTimeHandler recoverTimeHandler = PowerMockito.spy(new RecoverTimeHandler(mockRecoverTimeMetrics));
    Run currentBuild = new MockBuildBuilder().create();
    Run previousBuild = currentBuild.getPreviousBuild();

    doReturn(mockRecoveryTimeChild).when(mockRecoverTimeMetrics).labels(labels);
    PowerMockito.when(BuildUtil.isFirstSuccessfulBuildAfterError(currentBuild)).thenReturn(true);
    PowerMockito.doReturn(1L).when(recoverTimeHandler, "calculateRecoverTime", previousBuild, currentBuild);

    recoverTimeHandler.accept(currentBuild);

    PowerMockito.verifyPrivate(recoverTimeHandler, times(1)).invoke("calculateRecoverTime", previousBuild, currentBuild);
    verify(mockRecoverTimeMetrics, times(1)).labels(labels);
    verify(mockRecoveryTimeChild, times(1)).set(1L);
  }

  @Test
  public void should_not_set_value_to_metrics_if_calculated_lead_time_is_negative() throws Exception {
    RecoverTimeHandler recoverTimeHandler = PowerMockito.spy(new RecoverTimeHandler(mockRecoverTimeMetrics));
    Run currentBuild = new MockBuildBuilder().create();
    Run previousBuild = currentBuild.getPreviousBuild();

    doReturn(mockRecoveryTimeChild).when(mockRecoverTimeMetrics).labels(labels);
    PowerMockito.when(BuildUtil.isFirstSuccessfulBuildAfterError(currentBuild)).thenReturn(true);
    PowerMockito.doReturn(-1L).when(recoverTimeHandler, "calculateRecoverTime", previousBuild, currentBuild);

    recoverTimeHandler.accept(currentBuild);

    PowerMockito.verifyPrivate(recoverTimeHandler, times(1)).invoke("calculateRecoverTime", previousBuild, currentBuild);
    verify(mockRecoverTimeMetrics, times(0)).labels(labels);
    verify(mockRecoveryTimeChild, times(0)).set(1L);
  }

  @Test
  public void should_do_nothing_when_call_accept_given_a_unsuccessful_build() throws Exception {
    RecoverTimeHandler recoverTimeHandler = PowerMockito.spy(new RecoverTimeHandler(mockRecoverTimeMetrics));
    Run currentBuild = new MockBuildBuilder().result(Result.FAILURE).create();

    recoverTimeHandler.accept(currentBuild);

    PowerMockito.verifyPrivate(recoverTimeHandler, never()).invoke("calculateRecoverTime", currentBuild.getPreviousBuild(), currentBuild);
    verify(mockRecoverTimeMetrics, never()).labels(LEADTIME_HANDLER_LABELS);
    verify(mockRecoveryTimeChild, never()).set(anyLong());
  }

  @Test
  public void should_return_recovery_time_given_a_success_build_after_a_failed_build() {
    MockBuild previousBuild = new MockBuildBuilder().startTimeInMillis(70).duration(20).result(Result.FAILURE).previousBuild(null).create();
    MockBuild lastBuild = previousBuild.createNextBuild(30L, 50L, Result.SUCCESS);
    Mockito.when(mockRecoverTimeMetrics.labels(any())).thenReturn(mockRecoveryTimeChild);

    recoverTimeHandler.accept(lastBuild);

    Mockito.verify(mockRecoverTimeMetrics, Mockito.times(1)).labels(labels);
    Mockito.verify(mockRecoveryTimeChild, Mockito.times(1)).set(60);
  }

  @Test
  public void should_recovery_first_failed_build_given_a_success_build_after_two_failed_build() {
    MockBuild previousPreviousBuild = new MockBuildBuilder().startTimeInMillis(70).duration(20).result(Result.FAILURE).previousBuild(null).create();
    MockBuild previousBuild = previousPreviousBuild.createNextBuild(30L, 50L, Result.FAILURE);
    MockBuild lastBuild = previousBuild.createNextBuild(30L, 50L, Result.SUCCESS);
    Mockito.when(mockRecoverTimeMetrics.labels(any())).thenReturn(mockRecoveryTimeChild);

    recoverTimeHandler.accept(lastBuild);

    Long exceptedRecoveryTime = BuildUtil.getBuildEndTime(lastBuild) - BuildUtil.getBuildEndTime(previousPreviousBuild);
    Mockito.verify(mockRecoverTimeMetrics, Mockito.times(1)).labels(labels);
    Mockito.verify(mockRecoveryTimeChild, Mockito.times(1)).set(exceptedRecoveryTime);
  }

  @Test
  public void should_recovery_first_failed_build_given_a_later_success_build_completed_first_after_failed_build() {
    MockBuild previousPreviousBuild = new MockBuildBuilder().startTimeInMillis(70).duration(20).result(Result.FAILURE).previousBuild(null).create();
    MockBuild previousBuild = previousPreviousBuild.createNextBuild(30L, 150L, Result.SUCCESS);
    MockBuild lastBuild = previousBuild.createNextBuild(30L, 50L, Result.SUCCESS);
    Mockito.when(mockRecoverTimeMetrics.labels(any())).thenReturn(mockRecoveryTimeChild);

    recoverTimeHandler.accept(lastBuild);

    Long exceptedRecoveryTime = BuildUtil.getBuildEndTime(lastBuild) - BuildUtil.getBuildEndTime(previousPreviousBuild);
    Mockito.verify(mockRecoverTimeMetrics, Mockito.times(1)).labels(labels);
    Mockito.verify(mockRecoveryTimeChild, Mockito.times(1)).set(exceptedRecoveryTime);
  }

  @Test
  public void should_not_send_recoveryTime_given_a_success_build_after_a_success_build() {
    MockBuild previousBuild = new MockBuildBuilder().startTimeInMillis(70).duration(20).result(Result.SUCCESS).previousBuild(null).create();
    MockBuild lastBuild = previousBuild.createNextBuild(30L, 50L, Result.SUCCESS);
    Mockito.when(mockRecoverTimeMetrics.labels(any())).thenReturn(mockRecoveryTimeChild);

    recoverTimeHandler.accept(lastBuild);

    Mockito.verify(mockRecoverTimeMetrics, Mockito.times(0)).labels(labels);
    Assert.assertEquals(Long.valueOf(Long.MIN_VALUE), recoverTimeHandler.calculateRecoverTime(lastBuild.getPreviousBuild(), lastBuild));
  }

  @Test
  public void should_not_send_recoveryTime_given_previous_build_is_completed_after_current_build() {
    MockBuild previousBuild = new MockBuildBuilder().startTimeInMillis(70).duration(100).result(Result.SUCCESS).previousBuild(null).create();
    MockBuild lastBuild = previousBuild.createNextBuild(10L, 50L, Result.SUCCESS);
    Mockito.when(mockRecoverTimeMetrics.labels(any())).thenReturn(mockRecoveryTimeChild);

    recoverTimeHandler.accept(lastBuild);

    Mockito.verify(mockRecoverTimeMetrics, Mockito.times(0)).labels(labels);
  }

  @Test
  public void should_not_send_recoveryTime_given_a_failed_build() {
    MockBuild lastBuild = new MockBuildBuilder().startTimeInMillis(70).duration(100).result(Result.FAILURE).previousBuild(null).create();
    Mockito.when(mockRecoverTimeMetrics.labels(any())).thenReturn(mockRecoveryTimeChild);

    recoverTimeHandler.accept(lastBuild);

    Mockito.verify(mockRecoverTimeMetrics, Mockito.times(0)).labels(labels);
  }

  @Test
  public void should_not_send_recoveryTime_given_a_first_success_build() {
    MockBuild lastBuild = new MockBuildBuilder().startTimeInMillis(70).duration(100).result(Result.SUCCESS).previousBuild(null).create();
    Mockito.when(mockRecoverTimeMetrics.labels(any())).thenReturn(mockRecoveryTimeChild);

    recoverTimeHandler.accept(lastBuild);

    Mockito.verify(mockRecoverTimeMetrics, Mockito.times(0)).labels(labels);
    Assert.assertEquals(Long.valueOf(Long.MIN_VALUE), recoverTimeHandler.calculateRecoverTime(lastBuild.getPreviousBuild(), lastBuild));
  }

}
