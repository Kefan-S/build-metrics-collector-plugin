package io.jenkins.plugins.collector.service;

import com.google.inject.Inject;
import io.jenkins.plugins.collector.data.JobCollector;
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

  @Inject
  public DefaultPrometheusMetrics(JobCollector jobCollector) {
    this.collectorRegistry = CollectorRegistry.defaultRegistry;
    this.cachedMetrics = new AtomicReference<>("");
    collectorRegistry.register(jobCollector);
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
