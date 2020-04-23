package io.jenkins.plugins.collector.service;

import com.google.inject.Inject;
import hudson.Extension;
import hudson.init.Initializer;
import io.jenkins.plugins.collector.config.PrometheusConfiguration;
import java.util.Timer;
import java.util.concurrent.TimeUnit;
import jenkins.model.Jenkins;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static hudson.init.InitMilestone.JOB_LOADED;

@Extension
public class AsyncWorkerManager {

  private static final Logger logger = LoggerFactory.getLogger(AsyncWorkerManager.class);

  private Timer timer;
  private AsyncWork timerTask;
  private PrometheusMetrics prometheusMetrics;

  public AsyncWorkerManager() {

  }

  @Inject
  public void setJobCollector(PrometheusMetrics prometheusMetrics) {
    this.prometheusMetrics = prometheusMetrics;
  }

  @Initializer(after= JOB_LOADED)
  public void init() {
    timer = new Timer("prometheus collector");
    timerTask = new AsyncWork(prometheusMetrics);
    timer.schedule(timerTask, 0, TimeUnit.SECONDS.toMillis(PrometheusConfiguration.get().getCollectingMetricsPeriodInSeconds()));
  }

  public static AsyncWorkerManager get() {
    return Jenkins.getInstanceOrNull().getExtensionList(AsyncWorkerManager.class).get(0);
  }

  public void updateAsyncWorker() {
    logger.info("start to update period");
    timerTask.cancel();
    timerTask = new AsyncWork(prometheusMetrics);
    long period = TimeUnit.SECONDS.toMillis(PrometheusConfiguration.get().getCollectingMetricsPeriodInSeconds());
    timer.schedule(timerTask, period, period);
    logger.info("update period successful!");
  }

}
