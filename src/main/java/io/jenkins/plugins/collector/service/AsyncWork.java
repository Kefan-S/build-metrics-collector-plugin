package io.jenkins.plugins.collector.service;

import io.jenkins.plugins.collector.model.BuildInfo;
import java.util.List;
import java.util.TimerTask;
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
    List<BuildInfo> buildInfoList = metricsService.getAllBuildInfo();
    prometheusMetrics.accept(buildInfoList);
    logger.info("Prometheus metrics collected successfully");
  }
}
