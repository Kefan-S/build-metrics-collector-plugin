package io.jenkins.plugins.collector.data;

import io.prometheus.client.Collector.MetricFamilySamples;
import io.prometheus.client.Collector.Type;
import io.prometheus.client.Gauge;
import java.util.List;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class CustomizeMetricsTest {
  private CustomizeMetrics customizeMetrics;

  @Before
  public void setUp() {
    customizeMetrics = new CustomizeMetrics();
  }

  @Test
  public void should_clear_all_collectors_when_initMetrics_given_collectors_is_not_empty() {

    Gauge gaugeMetric = Gauge.build().name("name").labelNames("labelName").help("help").create();
    customizeMetrics.addCollector(gaugeMetric);
    gaugeMetric.labels("label1").set(1);
    gaugeMetric.labels("label2").set(2);

    List<MetricFamilySamples> beforeMetricFamilySamples = gaugeMetric.collect();
    assertEquals(1, beforeMetricFamilySamples.size());
    assertEquals(2, beforeMetricFamilySamples.get(0).samples.size());

    customizeMetrics.initMetrics();

    List<MetricFamilySamples> afterMetricFamilySamples = gaugeMetric.collect();
    assertEquals(1, afterMetricFamilySamples.size());
    assertEquals("name", afterMetricFamilySamples.get(0).name);
    assertEquals(Type.GAUGE, afterMetricFamilySamples.get(0).type);
    assertEquals("help", afterMetricFamilySamples.get(0).help);
    assertEquals(0, afterMetricFamilySamples.get(0).samples.size());
  }

  @Test
  public void should_return_empty_list_when_getMetricsList_given_collectors_is_empty() {

    List<MetricFamilySamples> metricList = customizeMetrics.getMetricsList();
    assertEquals(0, metricList.size());
  }

  @Test
  public void should_return_metrics_list_when_getMetricsList_given_collectors_has_one_element() {

    Gauge gaugeMetric = Gauge.build().name("name").labelNames("labelName").help("help").create();
    gaugeMetric.labels("label1").set(1);
    gaugeMetric.labels("label2").set(2);
    customizeMetrics.addCollector(gaugeMetric);

    List<MetricFamilySamples> metricsList = customizeMetrics.getMetricsList();
    assertEquals(1, metricsList.size());
    assertEquals("name", metricsList.get(0).name);
    assertEquals(Type.GAUGE, metricsList.get(0).type);
    assertEquals("help", metricsList.get(0).help);
    assertEquals(2, metricsList.get(0).samples.size());
    assertEquals("name", metricsList.get(0).samples.get(0).name);
    assertEquals(1, metricsList.get(0).samples.get(0).value, 0.00000001);
    assertArrayEquals(new String[]{"labelName"}, metricsList.get(0).samples.get(0).labelNames.toArray());
    assertArrayEquals(new String[]{"label1"}, metricsList.get(0).samples.get(0).labelValues.toArray());
    assertEquals("name", metricsList.get(0).samples.get(1).name);
    assertEquals(2, metricsList.get(0).samples.get(1).value, 0.00000001);
    assertArrayEquals(new String[]{"labelName"}, metricsList.get(0).samples.get(1).labelNames.toArray());
    assertArrayEquals(new String[]{"label2"}, metricsList.get(0).samples.get(1).labelValues.toArray());
  }

  @Test
  public void should_return_metrics_list_when_getMetricsList_given_collectors_has_two_elements() {

    Gauge gaugeMetric1 = Gauge.build().name("name1").labelNames("labelName1").help("help1").create();
    Gauge gaugeMetric2 = Gauge.build().name("name2").labelNames("labelName2").help("help2").create();
    gaugeMetric1.labels("label1").set(1);
    gaugeMetric2.labels("label2").set(2);
    customizeMetrics.addCollector(gaugeMetric1);
    customizeMetrics.addCollector(gaugeMetric2);

    List<MetricFamilySamples> metricsList = customizeMetrics.getMetricsList();
    assertEquals(2, metricsList.size());
    assertEquals("name1", metricsList.get(0).name);
    assertEquals(Type.GAUGE, metricsList.get(0).type);
    assertEquals("help1", metricsList.get(0).help);
    assertEquals(1, metricsList.get(0).samples.size());
    assertEquals("name1", metricsList.get(0).samples.get(0).name);
    assertArrayEquals(new String[]{"labelName1"}, metricsList.get(0).samples.get(0).labelNames.toArray());
    assertArrayEquals(new String[]{"label1"}, metricsList.get(0).samples.get(0).labelValues.toArray());
    assertEquals(1, metricsList.get(0).samples.get(0).value, 0.00000001);

    assertEquals("name2", metricsList.get(1).name);
    assertEquals(Type.GAUGE, metricsList.get(1).type);
    assertEquals("help2", metricsList.get(1).help);
    assertEquals(1, metricsList.get(1).samples.size());
    assertEquals("name2", metricsList.get(1).samples.get(0).name);
    assertArrayEquals(new String[]{"labelName2"}, metricsList.get(1).samples.get(0).labelNames.toArray());
    assertArrayEquals(new String[]{"label2"}, metricsList.get(1).samples.get(0).labelValues.toArray());
    assertEquals(2, metricsList.get(1).samples.get(0).value, 0.00000001);
  }
}
