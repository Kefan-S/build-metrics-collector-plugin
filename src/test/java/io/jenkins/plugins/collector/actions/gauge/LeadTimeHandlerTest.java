package io.jenkins.plugins.collector.actions.gauge;

import hudson.model.Result;
import hudson.model.Run;
import io.jenkins.plugins.collector.builder.MockBuild;
import io.jenkins.plugins.collector.builder.MockBuildBuilder;
import io.jenkins.plugins.collector.util.BuildUtil;
import io.prometheus.client.Gauge;
import io.prometheus.client.Gauge.Child;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import static io.jenkins.plugins.collector.config.Constant.METRICS_LABEL_NAME_ARRAY;
import static org.junit.Assert.assertEquals;
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


  @Test
  public void should_do_nothing_if_current_build_is_not_first_successful_build_after_error() {
    Gauge leadTimeMetrics = Mockito.mock(Gauge.class);
    Child leadTimeMetricsChild = Mockito.mock(Child.class);
    LeadTimeHandler leadTimeHandler = Mockito.mock(LeadTimeHandler.class);
    Run currentBuild = Mockito.mock(Run.class);

    PowerMockito.mockStatic(BuildUtil.class);
    Whitebox.setInternalState(leadTimeHandler, "leadTimeMetrics", leadTimeMetrics);
    when(BuildUtil.isFirstSuccessfulBuildAfterError(currentBuild.getNextBuild(), currentBuild)).thenReturn(false);
    when(leadTimeHandler.calculateLeadTime(currentBuild.getNextBuild(), currentBuild)).thenReturn(1L);
    doCallRealMethod().when(leadTimeHandler).accept(LEADTIME_HANDLER_LABELS, currentBuild);
    doReturn(leadTimeMetricsChild).when(leadTimeMetrics).labels(LEADTIME_HANDLER_LABELS);

    leadTimeHandler.accept(METRICS_LABEL_NAME_ARRAY.toArray(new String[0]), currentBuild);

    verify(leadTimeHandler, never()).calculateLeadTime(currentBuild.getPreviousBuild(), currentBuild);
    verify(leadTimeMetrics, never()).labels(LEADTIME_HANDLER_LABELS);
    verify(leadTimeMetricsChild, never()).set(anyDouble());

  }

  @Test
  public void should_calculate_lead_time_if_current_build_is_first_successful_build_after_error() {
    Gauge leadTimeMetrics = Mockito.mock(Gauge.class);
    Child leadTimeMetricsChild = Mockito.mock(Child.class);
    LeadTimeHandler leadTimeHandler = Mockito.mock(LeadTimeHandler.class);
    Run currentBuild = Mockito.mock(Run.class);

    PowerMockito.mockStatic(BuildUtil.class);
    Whitebox.setInternalState(leadTimeHandler, "leadTimeMetrics", leadTimeMetrics);
    when(BuildUtil.isFirstSuccessfulBuildAfterError(currentBuild.getNextBuild(), currentBuild)).thenReturn(true);
    when(leadTimeHandler.calculateLeadTime(currentBuild.getPreviousBuild(), currentBuild)).thenReturn(1L);
    doCallRealMethod().when(leadTimeHandler).accept(LEADTIME_HANDLER_LABELS, currentBuild);
    doReturn(leadTimeMetricsChild).when(leadTimeMetrics).labels(LEADTIME_HANDLER_LABELS);

    leadTimeHandler.accept(LEADTIME_HANDLER_LABELS, currentBuild);

    verify(leadTimeHandler, times(1)).calculateLeadTime(currentBuild.getPreviousBuild(), currentBuild);
    verify(leadTimeMetrics, times(1)).labels(LEADTIME_HANDLER_LABELS);
    verify(leadTimeMetricsChild, times(1)).set(1L);
  }

  @Test
  public void should_not_set_value_to_metrics_if_calculated_lead_time_is_negative() {
    Gauge leadTimeMetrics = Mockito.mock(Gauge.class);
    Child leadTimeMetricsChild = Mockito.mock(Child.class);
    LeadTimeHandler leadTimeHandler = Mockito.mock(LeadTimeHandler.class);
    Run currentBuild = Mockito.mock(Run.class);

    PowerMockito.mockStatic(BuildUtil.class);
    Whitebox.setInternalState(leadTimeHandler, "leadTimeMetrics", leadTimeMetrics);
    when(BuildUtil.isFirstSuccessfulBuildAfterError(currentBuild.getNextBuild(), currentBuild)).thenReturn(true);
    when(leadTimeHandler.calculateLeadTime(currentBuild.getPreviousBuild(), currentBuild)).thenReturn(-1L);
    doCallRealMethod().when(leadTimeHandler).accept(LEADTIME_HANDLER_LABELS, currentBuild);
    doReturn(leadTimeMetricsChild).when(leadTimeMetrics).labels(LEADTIME_HANDLER_LABELS);

    leadTimeHandler.accept(LEADTIME_HANDLER_LABELS, currentBuild);

    verify(leadTimeHandler, times(1)).calculateLeadTime(currentBuild.getPreviousBuild(), currentBuild);
    verify(leadTimeMetrics, times(0)).labels(LEADTIME_HANDLER_LABELS);
    verify(leadTimeMetricsChild, times(0)).set(1L);
  }

  @Test
  public void should_lead_time_be_duration_if_current_build_is_first_build() {
    Gauge leadTimeMetrics = Mockito.mock(Gauge.class);
    LeadTimeHandler leadTimeHandler = Mockito.spy(new LeadTimeHandler(leadTimeMetrics));
    MockBuild currentBuild = new MockBuildBuilder().startTimeInMillis(1000).duration(1000).result(Result.SUCCESS).create();
    Long calculateLeadTime = leadTimeHandler.calculateLeadTime(currentBuild.getPreviousBuild(), currentBuild);
    assertEquals(currentBuild.getDuration(), calculateLeadTime.longValue());
  }

  @Test
  public void should_lead_time_be_duration_if_there_is_a_unovertime_successful_build_before_current_build() {
    Gauge leadTimeMetrics = Mockito.mock(Gauge.class);
    LeadTimeHandler leadTimeHandler = Mockito.spy(new LeadTimeHandler(leadTimeMetrics));
    MockBuild currentBuild = new MockBuildBuilder().startTimeInMillis(3000).duration(1000).result(Result.SUCCESS).create();
    currentBuild.createPreviousBuild(1000L, 500L, Result.SUCCESS);
    Long calculateLeadTime = leadTimeHandler.calculateLeadTime(currentBuild.getPreviousBuild(), currentBuild);
    assertEquals(currentBuild.getDuration(), calculateLeadTime.longValue());
  }

  @Test
  public void should_lead_time_be_right_if_there_is_a_uncompleted_build_before_current_build() {
    Gauge leadTimeMetrics = Mockito.mock(Gauge.class);
    LeadTimeHandler leadTimeHandler = Mockito.spy(new LeadTimeHandler(leadTimeMetrics));
    MockBuild currentBuild = new MockBuildBuilder().startTimeInMillis(3000).duration(1000).result(Result.SUCCESS).create();
    MockBuild previousBuild = currentBuild.createPreviousBuild(1000L, 6000L, null);
    Long calculateLeadTime = leadTimeHandler.calculateLeadTime(currentBuild.getPreviousBuild(), currentBuild);
    long expected = currentBuild.getStartTimeInMillis() + currentBuild.getDuration() - previousBuild.getStartTimeInMillis();
    assertEquals(expected, calculateLeadTime.longValue());
  }

  @Test
  public void should_lead_time_be_right_if_there_is_a_unovertime_error_build_before_current_build() {
    Gauge leadTimeMetrics = Mockito.mock(Gauge.class);
    LeadTimeHandler leadTimeHandler = Mockito.spy(new LeadTimeHandler(leadTimeMetrics));
    MockBuild currentBuild = new MockBuildBuilder().startTimeInMillis(3000).duration(1000).result(Result.SUCCESS).create();
    MockBuild previousBuild = currentBuild.createPreviousBuild(1000L, 500L, Result.FAILURE);
    Long calculateLeadTime = leadTimeHandler.calculateLeadTime(currentBuild.getPreviousBuild(), currentBuild);
    long expected = currentBuild.getStartTimeInMillis() + currentBuild.getDuration() - previousBuild.getStartTimeInMillis();
    assertEquals(expected, calculateLeadTime.longValue());
  }

  @Test
  public void should_lead_time_be_right_if_there_is_a_overtime_error_build_before_current_build() {
    Gauge leadTimeMetrics = Mockito.mock(Gauge.class);
    LeadTimeHandler leadTimeHandler = Mockito.spy(new LeadTimeHandler(leadTimeMetrics));
    MockBuild currentBuild = new MockBuildBuilder().startTimeInMillis(3000).duration(1000).result(Result.SUCCESS).create();
    MockBuild previousBuild = currentBuild.createPreviousBuild(1000L, 50000L, Result.FAILURE);
    Long calculateLeadTime = leadTimeHandler.calculateLeadTime(currentBuild.getPreviousBuild(), currentBuild);
    long expected = currentBuild.getStartTimeInMillis() + currentBuild.getDuration() - previousBuild.getStartTimeInMillis();
    assertEquals(expected, calculateLeadTime.longValue());
  }
}
