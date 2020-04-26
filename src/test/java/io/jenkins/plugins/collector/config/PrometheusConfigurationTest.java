package io.jenkins.plugins.collector.config;

import hudson.model.Descriptor;
import hudson.model.Descriptor.FormException;
import io.jenkins.plugins.collector.service.AsyncWorkerManager;
import io.jenkins.plugins.collector.service.PeriodProvider;
import java.util.Collection;
import java.util.Collections;
import jenkins.model.Jenkins;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;
import org.kohsuke.stapler.StaplerRequest;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;
import org.powermock.reflect.Whitebox;

import static com.google.common.collect.ImmutableMap.of;
import static com.google.common.collect.Lists.newArrayList;
import static java.util.Collections.singletonMap;
import static net.sf.json.JSONObject.fromObject;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PowerMockRunnerDelegate(Parameterized.class)
@PrepareForTest(value = {AsyncWorkerManager.class, PeriodProvider.class, Jenkins.class})
public class PrometheusConfigurationTest {

  private PrometheusConfiguration prometheusConfiguration;

  @Parameter
  public JSONObject wrongFormatJobName;

  @Before
  public void setup() {
    prometheusConfiguration = Mockito.mock(PrometheusConfiguration.class);
    Mockito.doNothing().when((Descriptor) prometheusConfiguration).load();
  }

  @Parameters
  public static Collection<JSONObject> prepareData() {
    final String collectingMetricsPeriodInSeconds = "collectingMetricsPeriodInSeconds";
    final String jobName = "jobName";
    return newArrayList(
        fromObject(of(collectingMetricsPeriodInSeconds, 20L, jobName, "jobName1,jobName2,")),
        fromObject(of(collectingMetricsPeriodInSeconds, 20L, jobName, "jobName1,")),
        fromObject(of(collectingMetricsPeriodInSeconds, 20L, jobName, ",jobName1,")),
        fromObject(of(collectingMetricsPeriodInSeconds, 20L, jobName, ",jobName1")),
        fromObject(of(collectingMetricsPeriodInSeconds, 20L, jobName, ",jobName1,jobName2")),
        fromObject(of(collectingMetricsPeriodInSeconds, 20L, jobName, ",jobName1,jobName2,"))
    );
  }

  @Test
  public void should_set_default_collecting_metrics_period_in_seconds_when_set_period_given_null_collecting_seconds() {
    Mockito.doCallRealMethod().when(prometheusConfiguration).setCollectingMetricsPeriodInSeconds(any());
    when(prometheusConfiguration.getCollectingMetricsPeriodInSeconds()).thenCallRealMethod();

    prometheusConfiguration.setCollectingMetricsPeriodInSeconds(null);

    assertEquals(15L, prometheusConfiguration.getCollectingMetricsPeriodInSeconds());
  }

  @Test
  public void should_set_job_name_when_set_job_name_given_job_name() {
    Mockito.doCallRealMethod().when(prometheusConfiguration).setJobName(any());
    when(prometheusConfiguration.getJobName()).thenCallRealMethod();

    prometheusConfiguration.setJobName("jobName");

    assertEquals("jobName", prometheusConfiguration.getJobName());
  }

  @Test
  public void should_set_expected_collecting_metrics_period_in_seconds_when_set_period_given_no_null_collecting_seconds() {
    Mockito.doCallRealMethod().when(prometheusConfiguration).setCollectingMetricsPeriodInSeconds(any());
    when(prometheusConfiguration.getCollectingMetricsPeriodInSeconds()).thenCallRealMethod();

    prometheusConfiguration.setCollectingMetricsPeriodInSeconds(20L);

    assertEquals(20L, prometheusConfiguration.getCollectingMetricsPeriodInSeconds());
  }

  @Test
  public void should_return_true_and_update_async_work_and_period_when_call_configure_given_positive_collecting_seconds_and_value_has_been_changed() throws FormException {
    Mockito.when(prometheusConfiguration.configure(any(), any())).thenCallRealMethod();
    Whitebox.setInternalState(prometheusConfiguration, "collectingMetricsPeriodInSeconds", 100L);
    final JSONObject json = fromObject(of("collectingMetricsPeriodInSeconds", 20L, "jobName", "jobName1,jobName2"));
    StaplerRequest staplerRequest = Mockito.mock(StaplerRequest.class);
    Mockito.doNothing().when(staplerRequest).bindJSON(any(Object.class), any(JSONObject.class));

    PowerMockito.mockStatic(AsyncWorkerManager.class);
    AsyncWorkerManager mockAsyncWorkerManager = mock(AsyncWorkerManager.class);
    when(AsyncWorkerManager.get()).thenReturn(mockAsyncWorkerManager);

    PowerMockito.mockStatic(PeriodProvider.class);
    PeriodProvider mockPeriodProvider = mock(PeriodProvider.class);
    when(PeriodProvider.get()).thenReturn(mockPeriodProvider);

    boolean actual = prometheusConfiguration.configure(staplerRequest, json);

    assertTrue(actual);
    verify(mockAsyncWorkerManager).updateAsyncWorker();
    verify(mockPeriodProvider).updatePeriods();
  }

  @Test
  public void should_return_true_and_never_update_when_call_configure_given_positive_collecting_seconds_and_value_has_not_been_changed() throws FormException {
    when(prometheusConfiguration.configure(any(), any())).thenCallRealMethod();
    Whitebox.setInternalState(prometheusConfiguration, "collectingMetricsPeriodInSeconds", 20L);
    final JSONObject json = fromObject(of("collectingMetricsPeriodInSeconds", 20L, "jobName", "jobName"));
    StaplerRequest staplerRequest = Mockito.mock(StaplerRequest.class);
    Mockito.doNothing().when(staplerRequest).bindJSON(any(Object.class), any(JSONObject.class));

    PowerMockito.mockStatic(AsyncWorkerManager.class);
    AsyncWorkerManager mockAsyncWorkerManager = mock(AsyncWorkerManager.class);
    when(AsyncWorkerManager.get()).thenReturn(mockAsyncWorkerManager);

    PowerMockito.mockStatic(PeriodProvider.class);
    PeriodProvider mockPeriodProvider = mock(PeriodProvider.class);
    when(PeriodProvider.get()).thenReturn(mockPeriodProvider);

    boolean actual = prometheusConfiguration.configure(staplerRequest, json);

    assertTrue(actual);
    verify(mockAsyncWorkerManager, never()).updateAsyncWorker();
    verify(mockPeriodProvider, never()).updatePeriods();
  }

  @Test(expected = FormException.class)
  public void should_throws_FormException_when_call_configure_given_negative_collecting_seconds() throws Descriptor.FormException {
    when(prometheusConfiguration.configure(any(), any())).thenCallRealMethod();
    Whitebox.setInternalState(prometheusConfiguration, "collectingMetricsPeriodInSeconds", 20L);
    StaplerRequest staplerRequest = mock(StaplerRequest.class);
    JSONObject json = fromObject(singletonMap("collectingMetricsPeriodInSeconds", -20L));

    prometheusConfiguration.configure(staplerRequest, json);

    verify(prometheusConfiguration, times(0)).configure(staplerRequest, json);
    verify(staplerRequest, times(0)).bindJSON(prometheusConfiguration, json);
    verify(prometheusConfiguration, times(0)).save();
  }

  @Test(expected = FormException.class)
  public void should_throws_FormException_when_call_configure_given_wrong_format_job_name() throws Descriptor.FormException {
    when(prometheusConfiguration.configure(any(), any())).thenCallRealMethod();
    Whitebox.setInternalState(prometheusConfiguration, "collectingMetricsPeriodInSeconds", 20L);
    StaplerRequest staplerRequest = mock(StaplerRequest.class);

    prometheusConfiguration.configure(staplerRequest, this.wrongFormatJobName);
  }

  @Test
  public void should_return_true_when_call_configure_given_empty_job_name() throws Descriptor.FormException {
    when(prometheusConfiguration.configure(any(), any())).thenCallRealMethod();
    Whitebox.setInternalState(prometheusConfiguration, "collectingMetricsPeriodInSeconds", 20L);
    StaplerRequest staplerRequest = mock(StaplerRequest.class);

    final JSONObject jsonObject = fromObject(of("collectingMetricsPeriodInSeconds", 20L, "jobName", ""));
    final boolean actual = prometheusConfiguration.configure(staplerRequest, jsonObject);

    assertTrue(actual);
  }

  @Test(expected = JSONException.class)
  public void should_throws_JsonException_when_call_configure_given_json_which_does_not_contains_collectingMetricsPeriodInSeconds_field() throws FormException {
    when(prometheusConfiguration.configure(any(), any())).thenCallRealMethod();
    Whitebox.setInternalState(prometheusConfiguration, "collectingMetricsPeriodInSeconds", 20L);
    StaplerRequest staplerRequest = mock(StaplerRequest.class);
    JSONObject json = fromObject(Collections.emptyMap());

    prometheusConfiguration.configure(staplerRequest, json);

    verify(prometheusConfiguration, times(0)).configure(staplerRequest, json);
    verify(staplerRequest, times(0)).bindJSON(prometheusConfiguration, json);
    verify(prometheusConfiguration, times(0)).save();
  }
}
