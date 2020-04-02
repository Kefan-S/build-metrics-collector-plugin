package io.jenkins.plugins.collector;


import hudson.model.Run;
import io.jenkins.plugins.collector.config.PrometheusConfiguration;
import io.jenkins.plugins.collector.util.CustomizeMetrics;
import io.jenkins.plugins.collector.util.Jobs;
import io.prometheus.client.Collector;
import java.time.Instant;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
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
    Jobs.forEachJob(job -> {

      logger.debug("Determining if we are already appending metrics for job [{}]", job.getName());

      String jobFullName = job.getFullName();

      if (!job.isBuildable()) {
        logger.debug("job [{}] is disabled", jobFullName);
        return;
      }

      logger.debug("Job [{}] is not already added. Appending its metrics", job.getName());

      long end = Instant.now().toEpochMilli();
      List<Run> unhandledBuilds = uncompletedBuildsMap.getOrDefault(jobFullName, new LinkedList<>());
      long statisticalPeriod = TimeUnit.SECONDS.toMillis(PrometheusConfiguration.get().getCollectingMetricsPeriodInSeconds());
      unhandledBuilds.addAll(job.getBuilds().byTimestamp(end - statisticalPeriod, end));
      unhandledBuilds.stream()
              .filter(build -> !build.isBuilding())
              .findFirst().ifPresent(
              build -> {
                handleBuild(build);
                unhandledBuilds.remove(build);
              }
      );
      logger.info("{} unhandleList: {}",jobFullName, unhandledBuilds);
      uncompletedBuildsMap.put(jobFullName, unhandledBuilds);

    });

    return customizeMetrics.getMetricsList();
  }
}
