package io.jenkins.plugins.collector.handler;

import hudson.model.Run;
import io.prometheus.client.Gauge;
import io.prometheus.client.SimpleCollector;
import java.util.List;

public abstract class AbstractHandler {

  void processMetrics(List<Gauge> gauges, Run build, Long metricValue) {
    gauges.forEach(SimpleCollector::clear);
    setMetricValue(build, metricValue);
  }

  abstract void setMetricValue(Run build, Long metricValue);
}
