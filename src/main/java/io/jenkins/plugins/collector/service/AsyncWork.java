package io.jenkins.plugins.collector.service;

import com.google.inject.Inject;
import io.jenkins.plugins.collector.model.Metrics;
import java.util.List;
import java.util.TimerTask;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AsyncWork extends TimerTask {

  private static final Logger logger = LoggerFactory.getLogger(AsyncWork.class);

  private PrometheusMetrics prometheusMetrics;
  private MetricsService metricsService;

  public AsyncWork(PrometheusMetrics prometheusMetrics, MetricsService metricsService) {

    this.prometheusMetrics = prometheusMetrics;
    this.metricsService = metricsService;
  }

  @Override
  public void run() {
    logger.info("Collecting prometheus metrics");
    List<Metrics> metricsList = metricsService.getAllMetrics();
    prometheusMetrics.accept(metricsList);
    logger.info("Prometheus metrics collected successfully");
  }
}
