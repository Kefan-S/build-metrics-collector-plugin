package io.jenkins.plugins.collector.service;

import com.google.inject.Inject;
import io.jenkins.plugins.collector.adapter.PrometheusAdapter;
import io.jenkins.plugins.collector.model.BuildInfo;
import io.prometheus.client.Collector.MetricFamilySamples;
import io.prometheus.client.exporter.common.TextFormat;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultPrometheusMetrics implements PrometheusMetrics {

  private static final Logger logger = LoggerFactory.getLogger(DefaultPrometheusMetrics.class);
  private final AtomicReference<String> cachedMetrics;

  @Inject
  public DefaultPrometheusMetrics() {
    this.cachedMetrics = new AtomicReference<>("");
  }

  @Override
  public String getMetrics() {
    return cachedMetrics.get();
  }

  @Override
  public void accept(List<BuildInfo> metrics) {

    PrometheusAdapter prometheusAdapter = new PrometheusAdapter();
    List<MetricFamilySamples> metricFamilySamples = metrics
        .stream()
        .map(prometheusAdapter::adapt)
        .flatMap(Collection::stream)
        .collect(Collectors.toList());

    try (StringWriter buffer = new StringWriter()) {
      TextFormat.write004(buffer, Collections.enumeration(metricFamilySamples));
      cachedMetrics.set(buffer.toString());
    } catch (IOException e) {
      logger.debug("Unable to collect metrics");
    }
  }
}
