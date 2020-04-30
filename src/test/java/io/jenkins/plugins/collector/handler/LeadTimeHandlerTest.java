package io.jenkins.plugins.collector.handler;

import hudson.model.Result;
import hudson.model.Run;
import io.jenkins.plugins.collector.builder.MockBuild;
import io.jenkins.plugins.collector.builder.MockBuildBuilder;
import io.jenkins.plugins.collector.service.LeadTimeCalculate;
import io.jenkins.plugins.collector.util.BuildUtil;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static io.jenkins.plugins.collector.config.Constant.METRICS_LABEL_NAME_ARRAY;
import static io.jenkins.plugins.collector.util.BuildUtil.isSuccessfulBuild;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({BuildUtil.class, LeadTimeCalculate.class})
public class LeadTimeHandlerTest {


  public static final String[] LEADTIME_HANDLER_LABELS = METRICS_LABEL_NAME_ARRAY.toArray(new String[0]);

  @Before
  public void setUp() {
    PowerMockito.mockStatic(BuildUtil.class);
    when(BuildUtil.isCompleteOvertime(any(), any())).thenCallRealMethod();
    when(BuildUtil.getBuildEndTime(any())).thenCallRealMethod();
    when(BuildUtil.isFirstSuccessfulBuildAfterError(any())).thenCallRealMethod();
    when(BuildUtil.getLabels(any())).thenReturn(LEADTIME_HANDLER_LABELS);
    when(isSuccessfulBuild(any())).thenCallRealMethod();
    when(BuildUtil.isAbortBuild(any())).thenCallRealMethod();
  }

  @Test
  public void should_do_nothing_if_current_build_is_not_first_successful_build_after_error() throws Exception {
    LeadTimeCalculate leadTimeCalculate = PowerMockito.spy(new LeadTimeCalculate());
    Run currentBuild = new MockBuildBuilder().create();

    PowerMockito.when(BuildUtil.isFirstSuccessfulBuildAfterError(currentBuild)).thenReturn(false);
    PowerMockito.doReturn(1L).when(leadTimeCalculate, "calculateLeadTime", currentBuild);

    final Long actual = leadTimeCalculate.apply(currentBuild);

    assertNull(actual);
    PowerMockito.verifyPrivate(leadTimeCalculate, never()).invoke("calculateLeadTime", currentBuild);
  }

  @Test
  public void should_calculate_lead_time_if_current_build_is_first_successful_build_after_error() throws Exception {
    LeadTimeCalculate leadTimeCalculate = PowerMockito.spy(new LeadTimeCalculate());
    Run currentBuild = new MockBuildBuilder().create();

    PowerMockito.when(BuildUtil.isFirstSuccessfulBuildAfterError(currentBuild)).thenReturn(true);
    PowerMockito.doReturn(1L).when(leadTimeCalculate, "calculateLeadTime", currentBuild);

    final Long actual = leadTimeCalculate.apply(currentBuild);

    assertEquals(1L, actual.longValue());
    PowerMockito.verifyPrivate(leadTimeCalculate, times(1)).invoke("calculateLeadTime", currentBuild);
  }

  @Test
  public void should_return_different_metric_data_when_handle_different_build_given_different_build() throws Exception {
    LeadTimeCalculate leadTimeCalculate = PowerMockito.spy(new LeadTimeCalculate());
    Run currentBuild1 = new MockBuildBuilder().create();
    Run currentBuild2 = new MockBuildBuilder().create();

    PowerMockito.when(BuildUtil.isFirstSuccessfulBuildAfterError(currentBuild1)).thenReturn(true);
    PowerMockito.doReturn(1L).when(leadTimeCalculate, "calculateLeadTime", currentBuild1);
    PowerMockito.when(BuildUtil.isFirstSuccessfulBuildAfterError(currentBuild2)).thenReturn(true);
    PowerMockito.doReturn(2L).when(leadTimeCalculate, "calculateLeadTime", currentBuild2);

    final Long actual1 = leadTimeCalculate.apply(currentBuild1);
    final Long actual2 = leadTimeCalculate.apply(currentBuild2);

    assertEquals(1L, actual1.longValue());
    assertEquals(2L, actual2.longValue());
  }

  @Test
  public void should_do_nothing_when_call_accept_given_a_unsuccessful_build() throws Exception {
    LeadTimeCalculate leadTimeCalculate = PowerMockito.spy(new LeadTimeCalculate());
    Run currentBuild = new MockBuildBuilder().result(Result.FAILURE).create();
    leadTimeCalculate.apply(currentBuild);
    PowerMockito.verifyPrivate(leadTimeCalculate, never()).invoke("calculateLeadTime", currentBuild);
  }

  @Test
  public void should_lead_time_be_duration_of_current_build_if_current_build_is_first_build() {
    LeadTimeCalculate leadTimeCalculate = Mockito.spy(new LeadTimeCalculate());
    MockBuild currentBuild = new MockBuildBuilder().startTimeInMillis(1000).duration(1000).result(Result.SUCCESS).create();

    Long actual = leadTimeCalculate.apply(currentBuild);

    assertEquals(currentBuild.getDuration(), actual.longValue());
  }

  @Test
  public void should_lead_time_be_duration_of_current_build_if_there_is_a_overtime_abort_build_before_current_build() {
    LeadTimeCalculate leadTimeCalculate = Mockito.spy(new LeadTimeCalculate());
    MockBuild currentBuild = new MockBuildBuilder().startTimeInMillis(3000).duration(1000).result(Result.SUCCESS).create();
    currentBuild.createPreviousBuild(1000L, 500L, Result.ABORTED);

    Long actual = leadTimeCalculate.apply(currentBuild);

    assertEquals(currentBuild.getDuration(), actual.longValue());
  }

  @Test
  public void should_lead_time_be_duration_of_current_build_if_there_is_a_no_overtime_successful_build_before_current_build() {
    LeadTimeCalculate leadTimeCalculate = Mockito.spy(new LeadTimeCalculate());
    MockBuild currentBuild = new MockBuildBuilder().startTimeInMillis(3000).duration(1000).result(Result.SUCCESS).create();
    currentBuild.createPreviousBuild(1000L, 500L, Result.SUCCESS);

    Long actual = leadTimeCalculate.apply(currentBuild);

    assertEquals(currentBuild.getDuration(), actual.longValue());
  }

  @Test
  public void should_lead_time_be_right_if_there_is_a_uncompleted_build_before_current_build() {
    LeadTimeCalculate leadTimeCalculate = Mockito.spy(new LeadTimeCalculate());
    MockBuild currentBuild = new MockBuildBuilder().startTimeInMillis(3000).duration(1000).result(Result.SUCCESS).create();
    MockBuild previousUncompletedBuild = currentBuild.createPreviousBuild(1000L, 6000L, null);

    Long actual = leadTimeCalculate.apply(currentBuild);
    long expected = currentBuild.getStartTimeInMillis() + currentBuild.getDuration() - previousUncompletedBuild.getStartTimeInMillis();

    assertEquals(expected, actual.longValue());
  }

  @Test
  public void should_lead_time_be_right_if_there_is_a_no_overtime_error_build_before_current_build() {
    LeadTimeCalculate leadTimeCalculate = Mockito.spy(new LeadTimeCalculate());
    MockBuild currentBuild = new MockBuildBuilder().startTimeInMillis(3000).duration(1000).result(Result.SUCCESS).create();
    MockBuild previousUnovertimeErrorBuild = currentBuild.createPreviousBuild(1000L, 500L, Result.FAILURE);

    Long actual = leadTimeCalculate.apply(currentBuild);
    long expected = currentBuild.getStartTimeInMillis() + currentBuild.getDuration() - previousUnovertimeErrorBuild.getStartTimeInMillis();

    assertEquals(expected, actual.longValue());
  }

  @Test
  public void should_lead_time_be_right_if_there_is_a_overtime_error_build_before_current_build() {
    LeadTimeCalculate leadTimeCalculate = Mockito.spy(new LeadTimeCalculate());
    MockBuild currentBuild = new MockBuildBuilder().startTimeInMillis(3000).duration(1000).result(Result.SUCCESS).create();
    MockBuild previousOvertimeSuccessfulBuild = currentBuild.createPreviousBuild(1000L, 50000L, Result.FAILURE);

    Long actual = leadTimeCalculate.apply(currentBuild);
    long expected = currentBuild.getStartTimeInMillis() + currentBuild.getDuration() - previousOvertimeSuccessfulBuild.getStartTimeInMillis();

    assertEquals(expected, actual.longValue());
  }

  @Test
  public void should_consider_all_previous_build_whatever_their_status_until_meet_first_no_overtime_successful_build() {
    LeadTimeCalculate leadTimeCalculate = Mockito.spy(new LeadTimeCalculate());
    MockBuild currentBuild = new MockBuildBuilder().startTimeInMillis(6000).duration(1000).result(Result.SUCCESS).create();
    MockBuild firstUnsuccessfulBuild = currentBuild.createPreviousBuild(1000L, 10000L, Result.FAILURE)
        .createPreviousBuild(1000L, 500L, Result.FAILURE)
        .createPreviousBuild(1000L, 20000L, null);
    firstUnsuccessfulBuild.createPreviousBuild(1000L, 1000L, Result.SUCCESS);

    Long actual = leadTimeCalculate.apply(currentBuild);
    long expected = currentBuild.getStartTimeInMillis() + currentBuild.getDuration() - firstUnsuccessfulBuild.getStartTimeInMillis();

    assertEquals(expected, actual.longValue());
  }

  @Test
  public void should_overtime_successful_build_be_considered_while_calculate_lead_time() {
    LeadTimeCalculate leadTimeCalculate = Mockito.spy(new LeadTimeCalculate());
    MockBuild currentBuild = new MockBuildBuilder().startTimeInMillis(6000).duration(1000).result(Result.SUCCESS).create();
    MockBuild overtimeSuccessfulBuild = currentBuild.createPreviousBuild(1000L, 10000L, Result.FAILURE)
        .createPreviousBuild(1000L, 500L, Result.FAILURE)
        .createPreviousBuild(1000L, 20000L, null)
        .createPreviousBuild(1000L, 30000L, Result.SUCCESS);

    Long actual = leadTimeCalculate.apply(currentBuild);
    long expected = currentBuild.getStartTimeInMillis() + currentBuild.getDuration() - overtimeSuccessfulBuild.getStartTimeInMillis();

    assertEquals(expected, actual.longValue());
  }

  @Test
  public void should_ignore_abort_build_when_calculate_lead_time() {
    LeadTimeCalculate leadTimeCalculate = Mockito.spy(new LeadTimeCalculate());
    MockBuild currentBuild = new MockBuildBuilder().startTimeInMillis(6000).duration(1000).result(Result.SUCCESS).create();
    MockBuild firstUnsuccessfulBuild = currentBuild.createPreviousBuild(1000L, 10000L, Result.FAILURE)
        .createPreviousBuild(1000L, 500L, Result.FAILURE)
        .createPreviousBuild(1000L, 20000L, null);
    firstUnsuccessfulBuild.createPreviousBuild(1000L, 30000L, Result.ABORTED);

    Long actual = leadTimeCalculate.apply(currentBuild);
    long expected = currentBuild.getStartTimeInMillis() + currentBuild.getDuration() - firstUnsuccessfulBuild.getStartTimeInMillis();

    assertEquals(expected, actual.longValue());
  }


}
