package io.jenkins.plugins.collector.handler;

import hudson.model.Run;
import io.prometheus.client.SimpleCollector;
import java.util.Arrays;

public abstract class AbstractHandler {

  void processMetrics(Run build, Long metricValue, SimpleCollector... simpleCollectors) {
    Arrays.stream(simpleCollectors)
        .forEach(SimpleCollector::clear);
    setMetricValue(build, metricValue);
  }

  abstract void setMetricValue(Run build, Long metricValue);
}
