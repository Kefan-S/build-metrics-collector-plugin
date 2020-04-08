package io.jenkins.plugins.collector.actions.gauge;

import hudson.model.Result;
import hudson.model.Run;
import io.jenkins.plugins.collector.builder.MockBuild;
import io.jenkins.plugins.collector.builder.MockBuildBuilder;
import io.jenkins.plugins.collector.util.BuildUtil;
import io.prometheus.client.Gauge;
import io.prometheus.client.Gauge.Child;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import static io.jenkins.plugins.collector.config.Constant.METRICS_LABEL_NAME_ARRAY;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest(BuildUtil.class)
public class LeadTimeHandlerTest {


  public static final String[] LEADTIME_HANDLER_LABELS = METRICS_LABEL_NAME_ARRAY.toArray(new String[0]);
  private Gauge leadTimeMetrics;
  private Child leadTimeMetricsChild;

  @Before
  public void setUp() {
    PowerMockito.mockStatic(BuildUtil.class);
    leadTimeMetrics = Mockito.mock(Gauge.class);
    leadTimeMetricsChild = Mockito.mock(Child.class);
    doReturn(leadTimeMetricsChild).when(leadTimeMetrics).labels(LEADTIME_HANDLER_LABELS);
    when(BuildUtil.isCompleteOvertime(any(), any())).thenCallRealMethod();
    when(BuildUtil.getBuildEndTime(any())).thenCallRealMethod();
  }

  @Test
  public void should_do_nothing_if_current_build_is_not_first_successful_build_after_error() {
    LeadTimeHandler leadTimeHandler = Mockito.mock(LeadTimeHandler.class);
    Run currentBuild = Mockito.mock(Run.class);

    Whitebox.setInternalState(leadTimeHandler, "leadTimeMetrics", leadTimeMetrics);
    when(BuildUtil.isFirstSuccessfulBuildAfterError(currentBuild.getNextBuild(), currentBuild)).thenReturn(false);
    when(leadTimeHandler.calculateLeadTime(currentBuild.getNextBuild(), currentBuild)).thenReturn(1L);
    doCallRealMethod().when(leadTimeHandler).accept(LEADTIME_HANDLER_LABELS, currentBuild);

    leadTimeHandler.accept(METRICS_LABEL_NAME_ARRAY.toArray(new String[0]), currentBuild);

    verify(leadTimeHandler, never()).calculateLeadTime(currentBuild.getPreviousBuild(), currentBuild);
    verify(leadTimeMetrics, never()).labels(LEADTIME_HANDLER_LABELS);
    verify(leadTimeMetricsChild, never()).set(anyDouble());

  }

  @Test
  public void should_calculate_lead_time_if_current_build_is_first_successful_build_after_error() {
    LeadTimeHandler leadTimeHandler = Mockito.mock(LeadTimeHandler.class);
    Run currentBuild = Mockito.mock(Run.class);

    Whitebox.setInternalState(leadTimeHandler, "leadTimeMetrics", leadTimeMetrics);
    when(BuildUtil.isFirstSuccessfulBuildAfterError(currentBuild.getNextBuild(), currentBuild)).thenReturn(true);
    when(leadTimeHandler.calculateLeadTime(currentBuild.getPreviousBuild(), currentBuild)).thenReturn(1L);
    doCallRealMethod().when(leadTimeHandler).accept(LEADTIME_HANDLER_LABELS, currentBuild);

    leadTimeHandler.accept(LEADTIME_HANDLER_LABELS, currentBuild);

    verify(leadTimeHandler, times(1)).calculateLeadTime(currentBuild.getPreviousBuild(), currentBuild);
    verify(leadTimeMetrics, times(1)).labels(LEADTIME_HANDLER_LABELS);
    verify(leadTimeMetricsChild, times(1)).set(1L);
  }

  @Test
  public void should_lead_time_be_duration_of_current_build_if_current_build_is_first_build() {
    LeadTimeHandler leadTimeHandler = Mockito.spy(new LeadTimeHandler(leadTimeMetrics));
    MockBuild currentBuild = new MockBuildBuilder().startTimeInMillis(1000).duration(1000).result(Result.SUCCESS).create();
    when(BuildUtil.isFirstSuccessfulBuildAfterError(currentBuild.getNextBuild(), currentBuild)).thenReturn(true);

    leadTimeHandler.accept(LEADTIME_HANDLER_LABELS, currentBuild);

    verify(leadTimeMetricsChild, times(1)).set(currentBuild.getDuration());
  }

  @Test
  public void should_lead_time_be_duration_of_current_build_if_there_is_a_overtime_abort_build_before_current_build() {
    LeadTimeHandler leadTimeHandler = Mockito.spy(new LeadTimeHandler(leadTimeMetrics));
    MockBuild currentBuild = new MockBuildBuilder().startTimeInMillis(3000).duration(1000).result(Result.SUCCESS).create();
    currentBuild.createPreviousBuild(1000L, 500L, Result.ABORTED);
    when(BuildUtil.isFirstSuccessfulBuildAfterError(currentBuild.getNextBuild(), currentBuild)).thenReturn(true);

    leadTimeHandler.accept(LEADTIME_HANDLER_LABELS, currentBuild);

    verify(leadTimeMetricsChild, times(1)).set(currentBuild.getDuration());
  }

  @Test
  public void should_lead_time_be_duration_of_current_build_if_there_is_a_no_overtime_successful_build_before_current_build() {
    LeadTimeHandler leadTimeHandler = Mockito.spy(new LeadTimeHandler(leadTimeMetrics));
    MockBuild currentBuild = new MockBuildBuilder().startTimeInMillis(3000).duration(1000).result(Result.SUCCESS).create();
    currentBuild.createPreviousBuild(1000L, 500L, Result.SUCCESS);
    when(BuildUtil.isFirstSuccessfulBuildAfterError(currentBuild.getNextBuild(), currentBuild)).thenReturn(true);

    leadTimeHandler.accept(LEADTIME_HANDLER_LABELS, currentBuild);

    verify(leadTimeMetricsChild, times(1)).set(currentBuild.getDuration());
  }

  @Test
  public void should_lead_time_be_right_if_there_is_a_uncompleted_build_before_current_build() {
    LeadTimeHandler leadTimeHandler = Mockito.spy(new LeadTimeHandler(leadTimeMetrics));
    MockBuild currentBuild = new MockBuildBuilder().startTimeInMillis(3000).duration(1000).result(Result.SUCCESS).create();
    MockBuild previousUncompletedBuild = currentBuild.createPreviousBuild(1000L, 6000L, null);
    when(BuildUtil.isFirstSuccessfulBuildAfterError(currentBuild.getNextBuild(), currentBuild)).thenReturn(true);

    leadTimeHandler.accept(LEADTIME_HANDLER_LABELS, currentBuild);
    long expected = currentBuild.getStartTimeInMillis() + currentBuild.getDuration() - previousUncompletedBuild.getStartTimeInMillis();

    verify(leadTimeMetricsChild, times(1)).set(expected);
  }

  @Test
  public void should_lead_time_be_right_if_there_is_a_no_overtime_error_build_before_current_build() {
    LeadTimeHandler leadTimeHandler = Mockito.spy(new LeadTimeHandler(leadTimeMetrics));
    MockBuild currentBuild = new MockBuildBuilder().startTimeInMillis(3000).duration(1000).result(Result.SUCCESS).create();
    MockBuild previousUnovertimeErrorBuild = currentBuild.createPreviousBuild(1000L, 500L, Result.FAILURE);
    when(BuildUtil.isFirstSuccessfulBuildAfterError(currentBuild.getNextBuild(), currentBuild)).thenReturn(true);

    leadTimeHandler.accept(LEADTIME_HANDLER_LABELS, currentBuild);
    long expected = currentBuild.getStartTimeInMillis() + currentBuild.getDuration() - previousUnovertimeErrorBuild.getStartTimeInMillis();

    verify(leadTimeMetricsChild, times(1)).set(expected);
  }

  @Test
  public void should_lead_time_be_right_if_there_is_a_overtime_error_build_before_current_build() {
    LeadTimeHandler leadTimeHandler = Mockito.spy(new LeadTimeHandler(leadTimeMetrics));
    MockBuild currentBuild = new MockBuildBuilder().startTimeInMillis(3000).duration(1000).result(Result.SUCCESS).create();
    MockBuild previousOvertimeSuccessfulBuild = currentBuild.createPreviousBuild(1000L, 50000L, Result.FAILURE);
    when(BuildUtil.isFirstSuccessfulBuildAfterError(currentBuild.getNextBuild(), currentBuild)).thenReturn(true);

    leadTimeHandler.accept(LEADTIME_HANDLER_LABELS, currentBuild);
    long expected = currentBuild.getStartTimeInMillis() + currentBuild.getDuration() - previousOvertimeSuccessfulBuild.getStartTimeInMillis();

    verify(leadTimeMetricsChild, times(1)).set(expected);
  }

  @Test
  public void should_consider_all_previous_build_whatever_their_status_until_meet_first_no_overtime_successful_build() {
    LeadTimeHandler leadTimeHandler = Mockito.spy(new LeadTimeHandler(leadTimeMetrics));
    MockBuild currentBuild = new MockBuildBuilder().startTimeInMillis(6000).duration(1000).result(Result.SUCCESS).create();
    MockBuild firstUnsuccessfulBuild = currentBuild.createPreviousBuild(1000L, 10000L, Result.FAILURE)
        .createPreviousBuild(1000L, 500L, Result.FAILURE)
        .createPreviousBuild(1000L, 20000L, null);
    firstUnsuccessfulBuild.createPreviousBuild(1000L, 1000L, Result.SUCCESS);
    when(BuildUtil.isFirstSuccessfulBuildAfterError(currentBuild.getNextBuild(), currentBuild)).thenReturn(true);

    leadTimeHandler.accept(LEADTIME_HANDLER_LABELS, currentBuild);
    long expected = currentBuild.getStartTimeInMillis() + currentBuild.getDuration() - firstUnsuccessfulBuild.getStartTimeInMillis();


    verify(leadTimeMetricsChild, times(1)).set(expected);
  }

  @Test
  public void should_overtime_successful_build_be_considered_while_calculate_lead_time() {
    LeadTimeHandler leadTimeHandler = Mockito.spy(new LeadTimeHandler(leadTimeMetrics));
    MockBuild currentBuild = new MockBuildBuilder().startTimeInMillis(6000).duration(1000).result(Result.SUCCESS).create();
    MockBuild overtimeSuccessfulBuild = currentBuild.createPreviousBuild(1000L, 10000L, Result.FAILURE)
        .createPreviousBuild(1000L, 500L, Result.FAILURE)
        .createPreviousBuild(1000L, 20000L, null)
        .createPreviousBuild(1000L, 30000L, Result.SUCCESS);
    when(BuildUtil.isFirstSuccessfulBuildAfterError(currentBuild.getNextBuild(), currentBuild)).thenReturn(true);

    leadTimeHandler.accept(LEADTIME_HANDLER_LABELS, currentBuild);
    long expected = currentBuild.getStartTimeInMillis() + currentBuild.getDuration() - overtimeSuccessfulBuild.getStartTimeInMillis();

    verify(leadTimeMetricsChild, times(1)).set(expected);
  }

  @Test
  public void should_ignore_abort_build_when_calculate_lead_time() {
    LeadTimeHandler leadTimeHandler = Mockito.spy(new LeadTimeHandler(leadTimeMetrics));
    MockBuild currentBuild = new MockBuildBuilder().startTimeInMillis(6000).duration(1000).result(Result.SUCCESS).create();
    MockBuild firstUnsuccessfulBuild = currentBuild.createPreviousBuild(1000L, 10000L, Result.FAILURE)
        .createPreviousBuild(1000L, 500L, Result.FAILURE)
        .createPreviousBuild(1000L, 20000L, null);
    firstUnsuccessfulBuild.createPreviousBuild(1000L, 30000L, Result.ABORTED);
    when(BuildUtil.isFirstSuccessfulBuildAfterError(currentBuild.getNextBuild(), currentBuild)).thenReturn(true);

    leadTimeHandler.accept(LEADTIME_HANDLER_LABELS, currentBuild);
    long expected = currentBuild.getStartTimeInMillis() + currentBuild.getDuration() - firstUnsuccessfulBuild.getStartTimeInMillis();

    verify(leadTimeMetricsChild, times(1)).set(expected);
  }
}
