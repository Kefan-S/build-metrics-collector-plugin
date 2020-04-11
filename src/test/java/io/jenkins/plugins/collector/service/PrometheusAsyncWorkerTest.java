package io.jenkins.plugins.collector.service;

import hudson.model.TaskListener;
import io.jenkins.plugins.collector.config.PrometheusConfiguration;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest(PrometheusConfiguration.class)
public class PrometheusAsyncWorkerTest {

  @Test
  public void should_get_expected_millisecond_of_period_seconds_of_prometheus_configuration() {
    PowerMockito.mockStatic(PrometheusConfiguration.class);
    PrometheusConfiguration prometheusConfiguration = Mockito.mock(PrometheusConfiguration.class);
    PowerMockito.when(PrometheusConfiguration.get()).thenReturn(prometheusConfiguration);
    Mockito.when(prometheusConfiguration.getCollectingMetricsPeriodInSeconds()).thenReturn(15L);
    Assert.assertEquals(15000L, new PrometheusAsyncWorker().getRecurrencePeriod());
  }

  @Test
  public void should_execute_metric_collect_after_execute_method_has_been_called() {
    PrometheusAsyncWorker prometheusAsyncWorker = new PrometheusAsyncWorker();
    DefaultPrometheusMetrics prometheusMetrics = Mockito.mock(DefaultPrometheusMetrics.class);
    prometheusAsyncWorker.setPrometheusMetrics(prometheusMetrics);
    prometheusAsyncWorker.execute(Mockito.mock(TaskListener.class));
    Mockito.verify(prometheusMetrics, Mockito.times(1)).collectMetrics();
  }
}
