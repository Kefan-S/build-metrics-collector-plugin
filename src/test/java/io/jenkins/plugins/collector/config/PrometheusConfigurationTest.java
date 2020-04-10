package io.jenkins.plugins.collector.config;

import hudson.model.Descriptor.FormException;
import java.io.File;
import java.util.Collections;
import jenkins.model.Jenkins;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kohsuke.stapler.StaplerRequest;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Jenkins.class})
public class PrometheusConfigurationTest {

  @Before
  public void setUp() {
    PowerMockito.mockStatic(Jenkins.class);
    Jenkins jenkins = mock(Jenkins.class);
    Mockito.when(jenkins.getRootDir()).thenReturn(new File("."));
    PowerMockito.when(Jenkins.getInstance()).thenReturn(jenkins);
  }

  @Test
  public void should_set_default_collecting_metrics_period_in_seconds_when_set_period_given_null_collecting_seconds () {
    PrometheusConfiguration prometheusConfiguration = Mockito.spy(new PrometheusConfiguration());
    prometheusConfiguration.setCollectingMetricsPeriodInSeconds(null);
    Mockito.verify(prometheusConfiguration, times(1)).save();
    Assert.assertEquals(15L, prometheusConfiguration.getCollectingMetricsPeriodInSeconds());
  }

  @Test
  public void should_set_expected_collecting_metrics_period_in_seconds_when_set_period_given_no_null_collecting_seconds () {
    PrometheusConfiguration prometheusConfiguration = Mockito.spy(new PrometheusConfiguration());
    prometheusConfiguration.setCollectingMetricsPeriodInSeconds(20L);
    Mockito.verify(prometheusConfiguration, times(1)).save();
    Assert.assertEquals(20L, prometheusConfiguration.getCollectingMetricsPeriodInSeconds());
  }

  @Test
  public void should_set_expected_collecting_metrics_period_in_seconds_when_call_configure_given_positive_collecting_seconds () throws FormException {
    PrometheusConfiguration prometheusConfiguration = Mockito.spy(new PrometheusConfiguration());
    StaplerRequest staplerRequest = mock(StaplerRequest.class);
    JSONObject json = JSONObject.fromObject(Collections.singletonMap("collectingMetricsPeriodInSeconds", 20L));
    prometheusConfiguration.configure(staplerRequest, json);
    Mockito.verify(prometheusConfiguration, times(1)).configure(staplerRequest, json);
    Mockito.verify(staplerRequest, times(1)).bindJSON(prometheusConfiguration, json);
    Mockito.verify(prometheusConfiguration, times(1)).save();
    Assert.assertEquals(20L, prometheusConfiguration.getCollectingMetricsPeriodInSeconds());
  }

  @Test(expected = FormException.class)
  public void should_throws_FormException_when_call_configure_given_negative_collecting_seconds() throws FormException {
    PrometheusConfiguration prometheusConfiguration = Mockito.spy(new PrometheusConfiguration());
    StaplerRequest staplerRequest = mock(StaplerRequest.class);
    JSONObject json = JSONObject.fromObject(Collections.singletonMap("collectingMetricsPeriodInSeconds", -20L));
    prometheusConfiguration.configure(staplerRequest, json);
    Mockito.verify(prometheusConfiguration, times(0)).configure(staplerRequest, json);
    Mockito.verify(staplerRequest, times(0)).bindJSON(prometheusConfiguration, json);
    Mockito.verify(prometheusConfiguration, times(0)).save();
  }

  @Test(expected = JSONException.class)
  public void should_throws_JSONException_when_call_configure_given_json_which_does_not_contains_collectingMetricsPeriodInSeconds_field() throws FormException {
    PrometheusConfiguration prometheusConfiguration = Mockito.spy(new PrometheusConfiguration());
    StaplerRequest staplerRequest = mock(StaplerRequest.class);
    JSONObject json = JSONObject.fromObject(Collections.emptyMap());
    prometheusConfiguration.configure(staplerRequest, json);
    Mockito.verify(prometheusConfiguration, times(0)).configure(staplerRequest, json);
    Mockito.verify(staplerRequest, times(0)).bindJSON(prometheusConfiguration, json);
    Mockito.verify(prometheusConfiguration, times(0)).save();
  }
}
