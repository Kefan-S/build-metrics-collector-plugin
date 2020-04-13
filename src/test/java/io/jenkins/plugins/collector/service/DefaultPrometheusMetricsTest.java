package io.jenkins.plugins.collector.service;

import io.prometheus.client.Collector;
import io.prometheus.client.Collector.MetricFamilySamples;
import io.prometheus.client.Gauge;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.modules.junit4.PowerMockRunner;

import static io.jenkins.plugins.collector.config.Constant.METRICS_NAMESPACE;
import static io.jenkins.plugins.collector.config.Constant.METRICS_NAME_PREFIX;
import static io.jenkins.plugins.collector.config.Constant.METRICS_SUBSYSTEM;

@RunWith(PowerMockRunner.class)
public class DefaultPrometheusMetricsTest {

  @Test
  public void should_get_metrics_which_be_job_collector_collected() {
    Gauge leadTimeGauge = Gauge.build()
        .name(METRICS_NAME_PREFIX + "_merge_lead_time")
        .subsystem(METRICS_SUBSYSTEM).namespace(METRICS_NAMESPACE)
        .labelNames("labelName")
        .help("Code Merge Lead Time in milliseconds")
        .create();
    leadTimeGauge.labels("label").set(1L);
    List<MetricFamilySamples> metricFamilySamples = Stream.of(leadTimeGauge)
        .map(Collector::collect)
        .flatMap(Collection::stream)
        .collect(Collectors.toList());
    JobCollector jobCollector = Mockito.mock(JobCollector.class);
    Mockito.when(jobCollector.collect()).thenReturn(metricFamilySamples);
    DefaultPrometheusMetrics defaultPrometheusMetrics = new DefaultPrometheusMetrics(jobCollector);

    defaultPrometheusMetrics.collectMetrics();

    String metrics = "# HELP default_jenkins_builds_merge_lead_time Code Merge Lead Time in milliseconds\n" +
                     "# TYPE default_jenkins_builds_merge_lead_time gauge\n" +
                     "default_jenkins_builds_merge_lead_time{labelName=\"label\",} 1.0\n";
    Assert.assertEquals(metrics, defaultPrometheusMetrics.getMetrics());

  }
}
