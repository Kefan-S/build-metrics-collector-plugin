package io.jenkins.plugins.collector.consumer.prometheus;

import io.jenkins.plugins.collector.model.BuildInfo;
import io.jenkins.plugins.collector.model.TriggerInfo;
import io.prometheus.client.Collector.MetricFamilySamples;
import io.prometheus.client.Collector.MetricFamilySamples.Sample;
import java.util.List;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PrometheusAdapterTest {

  private static final Long DURATION = 1L;
  private static final Long LEAD_TIME = 2L;
  private static final Long RECOVER_TIME = 3L;
  private static final Long START_TIME = 4L;
  private PrometheusAdapter prometheusAdapter = new PrometheusAdapter();

  @Test
  public void should_return_prometheus_data_format_given_buildInfo_with_leadTime_and_recoveryTime() {
    BuildInfo buildInfo = BuildInfo.builder().duration(1L)
        .leadTime(LEAD_TIME)
        .recoveryTime(RECOVER_TIME)
        .startTime(START_TIME)
        .duration(DURATION)
        .jenkinsJob("name")
        .result("0")
        .triggerInfo(TriggerInfo.builder().triggeredBy("SCM").build())
        .build();

    List<MetricFamilySamples> metricFamilySamples = prometheusAdapter.adapt(buildInfo);

    Sample startTimeSample = metricFamilySamples.get(0).samples.get(0);
    Sample durationSample = metricFamilySamples.get(1).samples.get(0);
    Sample leadTimeSample = metricFamilySamples.get(2).samples.get(0);
    Sample recoveryTimeSample = metricFamilySamples.get(3).samples.get(0);
    Sample sample = metricFamilySamples.get(0).samples.get(0);

    assertEquals((double) START_TIME, startTimeSample.value, 0.001);
    assertEquals((double) DURATION, durationSample.value, 0.001);
    assertEquals((double) LEAD_TIME, leadTimeSample.value, 0.001);
    assertEquals((double) RECOVER_TIME, recoveryTimeSample.value, 0.001);
    assertEquals("name", sample.labelValues.get(0));
    assertEquals("SCM", sample.labelValues.get(1));
    assertEquals("0", sample.labelValues.get(2));

  }

  @Test
  public void should_return_prometheus_data_format_given_buildInfo_contains_recoveryTime() {
    BuildInfo buildInfo = BuildInfo.builder().duration(1L)
        .recoveryTime(RECOVER_TIME)
        .startTime(START_TIME)
        .duration(DURATION)
        .jenkinsJob("name")
        .result("0")
        .triggerInfo(TriggerInfo.builder().triggeredBy("SCM").build())
        .build();

    List<MetricFamilySamples> metricFamilySamples = prometheusAdapter.adapt(buildInfo);

    Sample recoveryTimeSample = metricFamilySamples.get(2).samples.get(0);
    assertEquals(metricFamilySamples.size(), 3);
    assertEquals((double) RECOVER_TIME, recoveryTimeSample.value, 0.001);
  }

  @Test
  public void should_return_prometheus_data_format_given_buildInfo_contains_leadTime() {
    BuildInfo buildInfo = BuildInfo.builder().duration(1L)
        .recoveryTime(LEAD_TIME)
        .startTime(START_TIME)
        .duration(DURATION)
        .jenkinsJob("name")
        .result("0")
        .triggerInfo(TriggerInfo.builder().triggeredBy("SCM").build())
        .build();

    List<MetricFamilySamples> metricFamilySamples = prometheusAdapter.adapt(buildInfo);

    Sample leadTimeSample = metricFamilySamples.get(2).samples.get(0);
    assertEquals(metricFamilySamples.size(), 3);
    assertEquals((double) LEAD_TIME, leadTimeSample.value, 0.001);
  }

  @Test
  public void should_return_prometheus_data_format_given_buildInfo_without_leadTime_or_recoveryTime() {
    BuildInfo buildInfo = BuildInfo.builder().duration(1L)
        .startTime(START_TIME)
        .duration(DURATION)
        .jenkinsJob("name")
        .result("0")
        .triggerInfo(TriggerInfo.builder().triggeredBy("SCM").build())
        .build();

    List<MetricFamilySamples> metricFamilySamples = prometheusAdapter.adapt(buildInfo);

    Sample startTimeSample = metricFamilySamples.get(0).samples.get(0);
    Sample durationSample = metricFamilySamples.get(1).samples.get(0);
    assertEquals((double) START_TIME, startTimeSample.value, 0.001);
    assertEquals((double) DURATION, durationSample.value, 0.001);
    assertEquals(metricFamilySamples.size(), 2);
  }
}
