package io.jenkins.plugins.collector.data;


import hudson.model.Run;
import io.jenkins.plugins.collector.config.PrometheusConfiguration;
import io.prometheus.client.Collector;
import java.time.Instant;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.jenkins.plugins.collector.actions.BuildMetricsCalculator.handleBuild;

public class JobCollector extends Collector {

  private static final Logger logger = LoggerFactory.getLogger(JobCollector.class);

  private Map<String, List<Run>> uncompletedBuildsMap = new HashMap<>();

  private CustomizeMetrics customizeMetrics;

  public JobCollector(CustomizeMetrics customizeMetrics) {
    this.customizeMetrics = customizeMetrics;
  }

  @Override
  public List<MetricFamilySamples> collect() {
    customizeMetrics.initMetrics();
    collectJob();
    return customizeMetrics.getMetricsList();
  }

  private void collectJob() {
    Jobs.forEachJob(job -> {
      logger.debug("Determining if we are already appending metrics for job [{}]", job.getName());

      if (!job.isBuildable()) {
        return;
      }

      String jobFullName = job.getFullName();
      long end = Instant.now().toEpochMilli();
      long statisticalPeriod = TimeUnit.SECONDS.toMillis(PrometheusConfiguration.get().getCollectingMetricsPeriodInSeconds());
      List<Run> unhandledBuilds = uncompletedBuildsMap.getOrDefault(jobFullName, new LinkedList<>());
      unhandledBuilds.addAll(job.getBuilds().byTimestamp(end - statisticalPeriod, end));

      collectBuild(unhandledBuilds);

      logger.info("{} unhandledList: {}", jobFullName, unhandledBuilds);
      uncompletedBuildsMap.put(jobFullName, unhandledBuilds);
    });
  }

  private void collectBuild(List<Run> unhandledBuilds) {
    Optional<Run> unhandledBuild = unhandledBuilds.stream()
        .filter(build -> !build.isBuilding())
        .findFirst();

    if (unhandledBuild.isPresent()) {
      handleBuild(unhandledBuild.get());
      unhandledBuilds.remove(unhandledBuild.get());
    }
  }
}
