package io.jenkins.plugins.collector.handler;

import hudson.model.Result;
import hudson.model.Run;
import io.jenkins.plugins.collector.builder.MockBuild;
import io.jenkins.plugins.collector.builder.MockBuildBuilder;
import io.jenkins.plugins.collector.util.BuildUtil;
import io.prometheus.client.Collector.MetricFamilySamples;
import io.prometheus.client.Gauge;
import io.prometheus.client.Gauge.Child;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static com.google.common.collect.Lists.newArrayList;
import static io.jenkins.plugins.collector.config.Constant.METRICS_LABEL_NAME_ARRAY;
import static io.jenkins.plugins.collector.util.BuildUtil.isSuccessfulBuild;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({BuildUtil.class, LeadTimeHandler.class})
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
    when(BuildUtil.isFirstSuccessfulBuildAfterError(any())).thenCallRealMethod();
    when(BuildUtil.getLabels(any())).thenReturn(LEADTIME_HANDLER_LABELS);
    when(isSuccessfulBuild(any())).thenCallRealMethod();
    when(BuildUtil.isAbortBuild(any())).thenCallRealMethod();
  }

  @Test
  public void should_do_nothing_if_current_build_is_not_first_successful_build_after_error() throws Exception {
    LeadTimeHandler leadTimeHandler = PowerMockito.spy(new LeadTimeHandler(leadTimeMetrics));
    Run currentBuild = new MockBuildBuilder().create();

    PowerMockito.when(BuildUtil.isFirstSuccessfulBuildAfterError(currentBuild)).thenReturn(false);
    PowerMockito.doReturn(1L).when(leadTimeHandler, "calculateLeadTime", currentBuild);

    final List<MetricFamilySamples> actual = leadTimeHandler.apply(currentBuild);

    assertEquals(0, actual.size());
    PowerMockito.verifyPrivate(leadTimeHandler, never()).invoke("calculateLeadTime", currentBuild);
    PowerMockito.verifyPrivate(leadTimeHandler, never()).invoke("setLeadTimeThenPush", currentBuild, 1L);
    verify(leadTimeMetrics, never()).clear();
    verify(leadTimeMetrics, never()).labels(LEADTIME_HANDLER_LABELS);
    verify(leadTimeMetricsChild, never()).set(1L);

  }

  @Test
  public void should_calculate_lead_time_if_current_build_is_first_successful_build_after_error() throws Exception {
    LeadTimeHandler leadTimeHandler = PowerMockito.spy(new LeadTimeHandler(leadTimeMetrics));
    Run currentBuild = new MockBuildBuilder().create();
    Run previousBuild = currentBuild.getPreviousBuild();

    PowerMockito.when(BuildUtil.isFirstSuccessfulBuildAfterError(currentBuild)).thenReturn(true);
    PowerMockito.doReturn(1L).when(leadTimeHandler, "calculateLeadTime", currentBuild);

    final MetricFamilySamples mockMetricFamilySamples = mock(MetricFamilySamples.class);
    when(leadTimeMetrics.collect()).thenReturn(newArrayList(mockMetricFamilySamples));

    final List<MetricFamilySamples> actual = leadTimeHandler.apply(currentBuild);

    assertEquals(newArrayList(mockMetricFamilySamples), actual);
    PowerMockito.verifyPrivate(leadTimeHandler, times(1)).invoke("calculateLeadTime", currentBuild);
    PowerMockito.verifyPrivate(leadTimeHandler, times(1)).invoke("setLeadTimeThenPush", currentBuild, 1L);
    verify(leadTimeMetrics, times(1)).clear();
    verify(leadTimeMetrics, times(1)).labels(LEADTIME_HANDLER_LABELS);
    verify(leadTimeMetricsChild, times(1)).set(1L);
  }

  @Test
  public void should_return_different_metric_data_when_handle_different_build_given_different_build() throws Exception {
    LeadTimeHandler leadTimeHandler = PowerMockito.spy(new LeadTimeHandler(leadTimeMetrics));
    Run currentBuild1 = new MockBuildBuilder().create();
    Run currentBuild2 = new MockBuildBuilder().create();

    PowerMockito.when(BuildUtil.isFirstSuccessfulBuildAfterError(currentBuild1)).thenReturn(true);
    PowerMockito.doReturn(1L).when(leadTimeHandler, "calculateLeadTime", currentBuild1);
    PowerMockito.when(BuildUtil.isFirstSuccessfulBuildAfterError(currentBuild2)).thenReturn(true);
    PowerMockito.doReturn(2L).when(leadTimeHandler, "calculateLeadTime", currentBuild2);
    final MetricFamilySamples mockMetricFamilySamples1 = mock(MetricFamilySamples.class);
    final MetricFamilySamples mockMetricFamilySamples2 = mock(MetricFamilySamples.class);
    when(leadTimeMetrics.collect()).thenReturn(newArrayList(mockMetricFamilySamples1)).thenReturn(newArrayList(mockMetricFamilySamples2));

    final List<MetricFamilySamples> actual1 = leadTimeHandler.apply(currentBuild1);
    final List<MetricFamilySamples> actual2 = leadTimeHandler.apply(currentBuild2);

    assertEquals(newArrayList(mockMetricFamilySamples1), actual1);
    assertEquals(newArrayList(mockMetricFamilySamples2), actual2);
  }

  @Test
  public void should_do_nothing_when_call_accept_given_a_unsuccessful_build() throws Exception {
    LeadTimeHandler leadTimeHandler = PowerMockito.spy(new LeadTimeHandler(leadTimeMetrics));
    Run currentBuild = new MockBuildBuilder().result(Result.FAILURE).create();
    leadTimeHandler.apply(currentBuild);
    PowerMockito.verifyPrivate(leadTimeHandler, never()).invoke("calculateLeadTime", currentBuild);
    verify(leadTimeMetrics, never()).labels(LEADTIME_HANDLER_LABELS);
    verify(leadTimeMetricsChild, never()).set(anyLong());
  }

  @Test
  public void should_lead_time_be_duration_of_current_build_if_current_build_is_first_build() {
    LeadTimeHandler leadTimeHandler = Mockito.spy(new LeadTimeHandler(leadTimeMetrics));
    MockBuild currentBuild = new MockBuildBuilder().startTimeInMillis(1000).duration(1000).result(Result.SUCCESS).create();

    leadTimeHandler.apply(currentBuild);

    verify(leadTimeMetricsChild, times(1)).set(currentBuild.getDuration());
  }

  @Test
  public void should_lead_time_be_duration_of_current_build_if_there_is_a_overtime_abort_build_before_current_build() {
    LeadTimeHandler leadTimeHandler = Mockito.spy(new LeadTimeHandler(leadTimeMetrics));
    MockBuild currentBuild = new MockBuildBuilder().startTimeInMillis(3000).duration(1000).result(Result.SUCCESS).create();
    currentBuild.createPreviousBuild(1000L, 500L, Result.ABORTED);

    leadTimeHandler.apply(currentBuild);

    verify(leadTimeMetricsChild, times(1)).set(currentBuild.getDuration());
  }

  @Test
  public void should_lead_time_be_duration_of_current_build_if_there_is_a_no_overtime_successful_build_before_current_build() {
    LeadTimeHandler leadTimeHandler = Mockito.spy(new LeadTimeHandler(leadTimeMetrics));
    MockBuild currentBuild = new MockBuildBuilder().startTimeInMillis(3000).duration(1000).result(Result.SUCCESS).create();
    currentBuild.createPreviousBuild(1000L, 500L, Result.SUCCESS);

    leadTimeHandler.apply(currentBuild);

    verify(leadTimeMetricsChild, times(1)).set(currentBuild.getDuration());
  }

  @Test
  public void should_lead_time_be_right_if_there_is_a_uncompleted_build_before_current_build() {
    LeadTimeHandler leadTimeHandler = Mockito.spy(new LeadTimeHandler(leadTimeMetrics));
    MockBuild currentBuild = new MockBuildBuilder().startTimeInMillis(3000).duration(1000).result(Result.SUCCESS).create();
    MockBuild previousUncompletedBuild = currentBuild.createPreviousBuild(1000L, 6000L, null);

    leadTimeHandler.apply(currentBuild);
    long expected = currentBuild.getStartTimeInMillis() + currentBuild.getDuration() - previousUncompletedBuild.getStartTimeInMillis();

    verify(leadTimeMetricsChild, times(1)).set(expected);
  }

  @Test
  public void should_lead_time_be_right_if_there_is_a_no_overtime_error_build_before_current_build() {
    LeadTimeHandler leadTimeHandler = Mockito.spy(new LeadTimeHandler(leadTimeMetrics));
    MockBuild currentBuild = new MockBuildBuilder().startTimeInMillis(3000).duration(1000).result(Result.SUCCESS).create();
    MockBuild previousUnovertimeErrorBuild = currentBuild.createPreviousBuild(1000L, 500L, Result.FAILURE);

    leadTimeHandler.apply(currentBuild);
    long expected = currentBuild.getStartTimeInMillis() + currentBuild.getDuration() - previousUnovertimeErrorBuild.getStartTimeInMillis();

    verify(leadTimeMetricsChild, times(1)).set(expected);
  }

  @Test
  public void should_lead_time_be_right_if_there_is_a_overtime_error_build_before_current_build() {
    LeadTimeHandler leadTimeHandler = Mockito.spy(new LeadTimeHandler(leadTimeMetrics));
    MockBuild currentBuild = new MockBuildBuilder().startTimeInMillis(3000).duration(1000).result(Result.SUCCESS).create();
    MockBuild previousOvertimeSuccessfulBuild = currentBuild.createPreviousBuild(1000L, 50000L, Result.FAILURE);

    leadTimeHandler.apply(currentBuild);
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

    leadTimeHandler.apply(currentBuild);
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

    leadTimeHandler.apply(currentBuild);
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

    leadTimeHandler.apply(currentBuild);
    long expected = currentBuild.getStartTimeInMillis() + currentBuild.getDuration() - firstUnsuccessfulBuild.getStartTimeInMillis();

    verify(leadTimeMetricsChild, times(1)).set(expected);
  }


}
