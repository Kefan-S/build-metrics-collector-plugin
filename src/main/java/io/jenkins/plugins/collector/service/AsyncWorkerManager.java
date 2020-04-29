package io.jenkins.plugins.collector.service;

import com.google.inject.Inject;
import hudson.Extension;
import hudson.init.Initializer;
import io.jenkins.plugins.collector.config.PrometheusConfiguration;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import jenkins.model.Jenkins;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static hudson.init.InitMilestone.EXTENSIONS_AUGMENTED;

@Extension
public class AsyncWorkerManager {

  private static final Logger logger = LoggerFactory.getLogger(AsyncWorkerManager.class);

  Timer timer;
  TimerTask timerTask;
  private PrometheusMetrics prometheusMetrics;
  private BuildInfoService buildInfoService;

  public AsyncWorkerManager() {

  }

  @Inject
  public void setPrometheusMetrics(PrometheusMetrics prometheusMetrics) {
    this.prometheusMetrics = prometheusMetrics;
  }

  @Inject
  public void setBuildInfoService(BuildInfoService buildInfoService) {
    this.buildInfoService = buildInfoService;
  }

  @Initializer(after = EXTENSIONS_AUGMENTED)
  public void init() {
    timer = new Timer("prometheus collector");
    timerTask = new AsyncWork(prometheusMetrics, buildInfoService);
    timer.schedule(timerTask, 0, TimeUnit.SECONDS.toMillis(PrometheusConfiguration.get().getCollectingMetricsPeriodInSeconds()));
  }

  public static AsyncWorkerManager get() {
    return Optional.ofNullable(Jenkins.getInstanceOrNull())
        .map(j -> j.getExtensionList(AsyncWorkerManager.class).get(0))
        .orElse(null);
  }

  public void updateAsyncWorker() {
    logger.info("start to update period");
    timerTask.cancel();
    timerTask = new AsyncWork(prometheusMetrics, buildInfoService);
    long period = TimeUnit.SECONDS.toMillis(PrometheusConfiguration.get().getCollectingMetricsPeriodInSeconds());
    timer.schedule(timerTask, period, period);
    logger.info("update period successful!");
  }

}
