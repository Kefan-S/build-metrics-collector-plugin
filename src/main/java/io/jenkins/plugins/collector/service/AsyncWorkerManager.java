package io.jenkins.plugins.collector.service;

import com.google.inject.Inject;
import hudson.Extension;
import hudson.init.Initializer;
import io.jenkins.plugins.collector.config.PrometheusConfiguration;
import io.jenkins.plugins.collector.model.BuildInfo;
import java.util.List;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import jenkins.model.Jenkins;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static hudson.init.InitMilestone.EXTENSIONS_AUGMENTED;

@Extension
public class AsyncWorkerManager {

  private static final Logger logger = LoggerFactory.getLogger(AsyncWorkerManager.class);

  Timer timer;
  TimerTask timerTask;
  private Consumer<List<BuildInfo>> consumers;
  private BuildInfoService buildInfoService;

  public AsyncWorkerManager() {

  }

  @Inject
  public void setConsumers(Consumer<List<BuildInfo>> consumers) {
    this.consumers = consumers;
  }

  @Inject
  public void setBuildInfoService(BuildInfoService buildInfoService) {
    this.buildInfoService = buildInfoService;
  }

  @Initializer(after = EXTENSIONS_AUGMENTED)
  public void init() {
    timer = new Timer("prometheus collector");
    timerTask = new AsyncWork(consumers, buildInfoService);
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
    timerTask = new AsyncWork(consumers, buildInfoService);
    long period = TimeUnit.SECONDS.toMillis(PrometheusConfiguration.get().getCollectingMetricsPeriodInSeconds());
    timer.schedule(timerTask, period, period);
    logger.info("update period successful!");
  }

}
