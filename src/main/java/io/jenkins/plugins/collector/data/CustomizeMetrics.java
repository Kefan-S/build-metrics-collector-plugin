package io.jenkins.plugins.collector.data;

import io.prometheus.client.Collector;
import io.prometheus.client.Collector.MetricFamilySamples;
import io.prometheus.client.SimpleCollector;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class CustomizeMetrics {

  private final List<SimpleCollector> collectors = new LinkedList<>();

  public void initMetrics() {
    collectors.forEach(SimpleCollector::clear);
  }

  public void addCollector(SimpleCollector collector) {
    collectors.add(collector);
  }

  public List<MetricFamilySamples> getMetricsList() {
    return collectors
        .stream()
        .map(Collector::collect)
        .flatMap(Collection::stream)
        .collect(Collectors.toList());
  }
}
