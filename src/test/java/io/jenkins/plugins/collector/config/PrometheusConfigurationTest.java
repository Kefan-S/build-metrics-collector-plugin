package io.jenkins.plugins.collector.config;

import hudson.model.Descriptor;
import hudson.model.Descriptor.FormException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.kohsuke.stapler.StaplerRequest;
import org.mockito.Mockito;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;

public class PrometheusConfigurationTest {

  private PrometheusConfiguration prometheusConfiguration;

  @Before
  public void setup() {
    prometheusConfiguration = Mockito.mock(PrometheusConfiguration.class);
    Mockito.doNothing().when((Descriptor) prometheusConfiguration).load();
  }

  @Test
  public void should_set_default_collecting_metrics_period_in_seconds_when_set_period_given_null_collecting_seconds() {
    Mockito.doCallRealMethod().when(prometheusConfiguration).setCollectingMetricsPeriodInSeconds(any());
    Mockito.when(prometheusConfiguration.getCollectingMetricsPeriodInSeconds()).thenCallRealMethod();

    prometheusConfiguration.setCollectingMetricsPeriodInSeconds(null);

    assertEquals(15L, prometheusConfiguration.getCollectingMetricsPeriodInSeconds());
  }

  @Test
  public void should_set_expected_collecting_metrics_period_in_seconds_when_set_period_given_no_null_collecting_seconds() {
    Mockito.doCallRealMethod().when(prometheusConfiguration).setCollectingMetricsPeriodInSeconds(any());
    Mockito.when(prometheusConfiguration.getCollectingMetricsPeriodInSeconds()).thenCallRealMethod();

    prometheusConfiguration.setCollectingMetricsPeriodInSeconds(20L);

    assertEquals(20L, prometheusConfiguration.getCollectingMetricsPeriodInSeconds());
  }

  @Test
  public void should_return_true_when_call_configure_given_positive_collecting_seconds() throws FormException {
    Mockito.when(prometheusConfiguration.configure(any(), any())).thenCallRealMethod();
    Map<String, Object> map = new HashMap<>();
    map.put("collectingMetricsPeriodInSeconds", 20L);
    map.put("jobName", "jobName1,jobName2");
    JSONObject json = JSONObject.fromObject(map);
    StaplerRequest staplerRequest = Mockito.mock(StaplerRequest.class);
    Mockito.doNothing().when(staplerRequest).bindJSON(any(Object.class), any(JSONObject.class));

    boolean actual = prometheusConfiguration.configure(staplerRequest, json);

    assertTrue(actual);
  }

  @Test(expected = FormException.class)
  public void should_throws_FormException_when_call_configure_given_negative_collecting_seconds() throws Descriptor.FormException {
    Mockito.when(prometheusConfiguration.configure(any(), any())).thenCallRealMethod();
    StaplerRequest staplerRequest = mock(StaplerRequest.class);
    JSONObject json = JSONObject.fromObject(Collections.singletonMap("collectingMetricsPeriodInSeconds", -20L));

    prometheusConfiguration.configure(staplerRequest, json);

    Mockito.verify(prometheusConfiguration, times(0)).configure(staplerRequest, json);
    Mockito.verify(staplerRequest, times(0)).bindJSON(prometheusConfiguration, json);
    Mockito.verify(prometheusConfiguration, times(0)).save();
  }

  @Test(expected = JSONException.class)
  public void should_throws_JsonException_when_call_configure_given_json_which_does_not_contains_collectingMetricsPeriodInSeconds_field() throws FormException {
    Mockito.when(prometheusConfiguration.configure(any(), any())).thenCallRealMethod();
    StaplerRequest staplerRequest = mock(StaplerRequest.class);
    JSONObject json = JSONObject.fromObject(Collections.emptyMap());

    prometheusConfiguration.configure(staplerRequest, json);

    Mockito.verify(prometheusConfiguration, times(0)).configure(staplerRequest, json);
    Mockito.verify(staplerRequest, times(0)).bindJSON(prometheusConfiguration, json);
    Mockito.verify(prometheusConfiguration, times(0)).save();
  }
}
