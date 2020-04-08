package io.jenkins.plugins.collector.service;

import io.jenkins.plugins.collector.data.JobCollector;
import io.jenkins.plugins.collector.data.CustomizeMetrics;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.exporter.common.TextFormat;
import java.io.IOException;
import java.io.StringWriter;
import java.util.concurrent.atomic.AtomicReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultPrometheusMetrics implements PrometheusMetrics {

  private static final Logger logger = LoggerFactory.getLogger(DefaultPrometheusMetrics.class);
  private final CollectorRegistry collectorRegistry;
  private final AtomicReference<String> cachedMetrics;

  public DefaultPrometheusMetrics(CustomizeMetrics customizeMetrics) {
    this.collectorRegistry = CollectorRegistry.defaultRegistry;
    this.cachedMetrics = new AtomicReference<>("");
    collectorRegistry.register(new JobCollector(customizeMetrics));
  }

  @Override
  public String getMetrics() {
    return cachedMetrics.get();
  }

  @Override
  public void collectMetrics() {
    try (StringWriter buffer = new StringWriter()) {
      TextFormat.write004(buffer, collectorRegistry.metricFamilySamples());
      cachedMetrics.set(buffer.toString());
    } catch (IOException e) {
      logger.debug("Unable to collect metrics");
    }
  }
}
