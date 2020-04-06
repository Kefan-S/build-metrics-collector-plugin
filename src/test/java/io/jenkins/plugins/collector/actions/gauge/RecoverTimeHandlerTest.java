package io.jenkins.plugins.collector.actions.gauge;

import hudson.model.Result;
import hudson.model.Run;
import io.jenkins.plugins.collector.builder.MockBuild;
import io.jenkins.plugins.collector.builder.MockBuildBuilder;
import io.jenkins.plugins.collector.util.BuildUtil;
import io.prometheus.client.Gauge;
import io.prometheus.client.Gauge.Child;
import java.util.Arrays;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;

import static io.jenkins.plugins.collector.config.Constant.METRICS_LABEL_NAME_ARRAY;
import static io.jenkins.plugins.collector.util.BuildUtil.isFirstSuccessfulBuildAfterError;
import static io.jenkins.plugins.collector.util.BuildUtil.isSuccessfulBuild;
import static org.mockito.ArgumentMatchers.any;

public class RecoverTimeHandlerTest {

  private RecoverTimeHandler recoverTimeHandler;

  private Gauge mockRecoverTimeMetrics;
  private Child mockRecoveryTimeChild;

  private String[] labels = (String[]) METRICS_LABEL_NAME_ARRAY.toArray();

  @Before
  public void setUp() {
    mockRecoveryTimeChild = Mockito.mock(Child.class);
    mockRecoverTimeMetrics = Mockito.mock(Gauge.class);
    recoverTimeHandler = new RecoverTimeHandler(mockRecoverTimeMetrics);
  }

  @Test
  public void should_return_recovery_time_given_a_success_build_after_a_failed_build() {
    MockBuild previousBuild = new MockBuildBuilder().startTimeInMillis(70).duration(20).result(Result.FAILURE).previousBuild(null).create();
    MockBuild lastBuild = previousBuild.createNextBuild(30L, 50L, Result.SUCCESS);
    Mockito.when(mockRecoverTimeMetrics.labels(any())).thenReturn(mockRecoveryTimeChild);

    recoverTimeHandler.accept(labels, lastBuild);

    Mockito.verify(mockRecoverTimeMetrics, Mockito.times(1)).labels(labels);
    Mockito.verify(mockRecoveryTimeChild, Mockito.times(1)).set(60);
  }

  @Test
  public void should_recovery_first_failed_build_given_a_success_build_after_two_failed_build() {
    MockBuild previousPreviousBuild = new MockBuildBuilder().startTimeInMillis(70).duration(20).result(Result.FAILURE).previousBuild(null).create();
    MockBuild previousBuild = previousPreviousBuild.createNextBuild(30L, 50L, Result.FAILURE);
    MockBuild lastBuild = previousBuild.createNextBuild(30L, 50L, Result.SUCCESS);
    Mockito.when(mockRecoverTimeMetrics.labels(any())).thenReturn(mockRecoveryTimeChild);

    recoverTimeHandler.accept(labels, lastBuild);

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

    recoverTimeHandler.accept(labels, lastBuild);

    Long exceptedRecoveryTime = BuildUtil.getBuildEndTime(lastBuild) - BuildUtil.getBuildEndTime(previousPreviousBuild);
    Mockito.verify(mockRecoverTimeMetrics, Mockito.times(1)).labels(labels);
    Mockito.verify(mockRecoveryTimeChild, Mockito.times(1)).set(exceptedRecoveryTime);
  }

  @Test
  public void should_not_send_recoveryTime_given_a_success_build_after_a_success_build() {
    MockBuild previousBuild = new MockBuildBuilder().startTimeInMillis(70).duration(20).result(Result.SUCCESS).previousBuild(null).create();
    MockBuild lastBuild = previousBuild.createNextBuild(30L, 50L, Result.SUCCESS);
    Mockito.when(mockRecoverTimeMetrics.labels(any())).thenReturn(mockRecoveryTimeChild);

    recoverTimeHandler.accept(labels, lastBuild);

    Mockito.verify(mockRecoverTimeMetrics, Mockito.times(0)).labels(labels);
    Assert.assertEquals(Long.valueOf(Long.MIN_VALUE), recoverTimeHandler.calculateRecoverTime(lastBuild.getPreviousBuild(), lastBuild));
  }

  @Test
  public void should_not_send_recoveryTime_given_previous_build_is_completed_after_current_build() {
    MockBuild previousBuild = new MockBuildBuilder().startTimeInMillis(70).duration(100).result(Result.SUCCESS).previousBuild(null).create();
    MockBuild lastBuild = previousBuild.createNextBuild(10L, 50L, Result.SUCCESS);
    Mockito.when(mockRecoverTimeMetrics.labels(any())).thenReturn(mockRecoveryTimeChild);

    recoverTimeHandler.accept(labels, lastBuild);

    Mockito.verify(mockRecoverTimeMetrics, Mockito.times(0)).labels(labels);
  }

  @Test
  public void should_not_send_recoveryTime_given_a_failed_build() {
    MockBuild lastBuild = new MockBuildBuilder().startTimeInMillis(70).duration(100).result(Result.FAILURE).previousBuild(null).create();
    Mockito.when(mockRecoverTimeMetrics.labels(any())).thenReturn(mockRecoveryTimeChild);

    recoverTimeHandler.accept(labels, lastBuild);

    Mockito.verify(mockRecoverTimeMetrics, Mockito.times(0)).labels(labels);
  }

  @Test
  public void should_not_send_recoveryTime_given_a_first_success_build() {
    MockBuild lastBuild = new MockBuildBuilder().startTimeInMillis(70).duration(100).result(Result.SUCCESS).previousBuild(null).create();
    Mockito.when(mockRecoverTimeMetrics.labels(any())).thenReturn(mockRecoveryTimeChild);

    recoverTimeHandler.accept(labels, lastBuild);

    Mockito.verify(mockRecoverTimeMetrics, Mockito.times(0)).labels(labels);
    Assert.assertEquals(Long.valueOf(Long.MIN_VALUE), recoverTimeHandler.calculateRecoverTime(lastBuild.getPreviousBuild(), lastBuild));
  }

  @Test(expected = IllegalArgumentException.class)
  public void should_throw_illegalArgumentException_given_a_build_with_wrong_labels() {
    MockBuild previousBuild = new MockBuildBuilder().startTimeInMillis(70).duration(20).result(Result.FAILURE).previousBuild(null).create();
    MockBuild lastBuild = previousBuild.createNextBuild(30L, 50L, Result.SUCCESS);

    Gauge recoveryGauge = Gauge.build("recovery_time", "recovery_time").labelNames(labels).create();
    String[] wrongLabels = Arrays.copyOf(labels, METRICS_LABEL_NAME_ARRAY.size() - 1);
    Mockito.when(recoveryGauge.labels(wrongLabels)).thenCallRealMethod().thenReturn(mockRecoveryTimeChild);

    new RecoverTimeHandler(recoveryGauge).accept(wrongLabels, lastBuild);

    Mockito.verify(recoveryGauge, Mockito.times(1)).labels(labels);
    Mockito.verify(mockRecoveryTimeChild, Mockito.times(0)).set(any());
  }
}
