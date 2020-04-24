package io.jenkins.plugins.collector.service;

import java.util.TimerTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AsyncWork extends TimerTask {

  private static final Logger logger = LoggerFactory.getLogger(AsyncWork.class);

  private PrometheusMetrics prometheusMetrics;

  public AsyncWork(PrometheusMetrics prometheusMetrics) {
    this.prometheusMetrics = prometheusMetrics;
  }

  @Override
  public void run() {
    logger.info("Collecting prometheus metrics");
    prometheusMetrics.collectMetrics();
    logger.info("Prometheus metrics collected successfully");
  }
}
