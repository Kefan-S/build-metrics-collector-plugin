package io.jenkins.plugins.collector;


import hudson.model.Result;
import hudson.model.Run;
import io.jenkins.plugins.collector.util.CachedBuilds;
import io.jenkins.plugins.collector.util.Jobs;
import io.prometheus.client.Collector;
import io.prometheus.client.Counter;
import io.prometheus.client.Gauge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class JobCollector extends Collector {

  private static final Logger logger = LoggerFactory.getLogger(JobCollector.class);

  private Gauge jobDuration;
  private Gauge jobResult;
  private Gauge jobStartTime;
  private Gauge jobRecoverTime;
  private Gauge jobLeadTime;
  private Counter jobSuccessCount;
  private Counter jobTotalCount;
  private Counter jobFailedCount;
  private CachedBuilds cachedBuilds = new CachedBuilds();

  @Override
  public List<MetricFamilySamples> collect() {

    String namespace = "default";
    List<MetricFamilySamples> samples = new ArrayList<>();
    String fullname = "builds";
    String subsystem = "jenkins";
    String[] labelNameArray = {"jenkins_job"};


    jobDuration = Gauge.build()
        .name(fullname + "_last_build_duration_in_milliseconds")
        .subsystem(subsystem).namespace(namespace)
        .labelNames(labelNameArray)
        .help("Last Build times in milliseconds")
        .create();

    jobStartTime = Gauge.build()
        .name(fullname + "_last_build_start_time_in_milliseconds")
        .subsystem(subsystem).namespace(namespace)
        .labelNames(labelNameArray)
        .help("Last Build Start Time in milliseconds")
        .create();

    jobResult = Gauge.build()
        .name(fullname + "_last_build_result_code")
        .subsystem(subsystem).namespace(namespace)
        .labelNames(labelNameArray)
        .help("Last Build Result")
        .create();

    jobRecoverTime = Gauge.build()
        .name(fullname + "_failed_build_recovery_time")
        .subsystem(subsystem).namespace(namespace)
        .labelNames(labelNameArray)
        .help("Failed Build Recovery Time in milliseconds")
        .create();

    jobLeadTime = Gauge.build()
        .name(fullname + "_merge_lead_time")
        .subsystem(subsystem).namespace(namespace)
        .labelNames(labelNameArray)
        .help("Code Merge Lead Time in milliseconds")
        .create();

    jobSuccessCount = Counter.build()
        .name(fullname + "_success_build_total_count")
        .subsystem(subsystem).namespace(namespace)
        .labelNames(labelNameArray)
        .help("Successful build count")
        .create();

    jobTotalCount = Counter.build()
        .name(fullname + "_build_total_count")
        .subsystem(subsystem).namespace(namespace)
        .labelNames(labelNameArray)
        .help("All build count")
        .create();

    jobFailedCount = Counter.build()
        .name(fullname + "_failed_build_total_count")
        .subsystem(subsystem).namespace(namespace)
        .labelNames(labelNameArray)
        .help("Failed build count")
        .create();

    Jobs.forEachJob(job -> {

      logger.debug("Determining if we are already appending metrics for job [{}]", job.getName());

      String jobFullName = job.getFullName();

      if (!job.isBuildable()) {
        logger.debug("job [{}] is disabled", jobFullName);
        return;
      }

      logger.debug("Job [{}] is not already added. Appending its metrics", job.getName());

      Run lastBuild = job.getLastBuild();
      if (lastBuild != null && lastBuild.getResult() != null) {
        setBuildMetricsForLastBuild(lastBuild, jobFullName);
      }
    });

    samples.addAll(jobDuration.collect());
    samples.addAll(jobStartTime.collect());
    samples.addAll(jobResult.collect());
    samples.addAll(jobRecoverTime.collect());
    samples.addAll(jobSuccessCount.collect());
    samples.addAll(jobFailedCount.collect());
    samples.addAll(jobLeadTime.collect());
    samples.addAll(jobTotalCount.collect());
    return samples;
  }

  private void setBuildMetricsForLastBuild(Run lastBuild, String jobFullName) {

    if (!updateLastBuildMap(jobFullName, lastBuild)) return;

    setRecoveryAndLeadTimeMetrics(jobFullName, lastBuild);

    jobDuration
        .labels(jobFullName)
        .set(lastBuild.getDuration());
    jobStartTime
        .labels(jobFullName)
        .set(lastBuild.getStartTimeInMillis());
    jobResult
        .labels(jobFullName)
        .set(lastBuild.getResult() == Result.SUCCESS ? 1 : 0);
    setFailedAndSuccessBuildsCount(lastBuild, jobFullName);
  }

  private void setFailedAndSuccessBuildsCount(Run lastBuild, String jobFullName) {
    while (lastBuild != null) {
      jobTotalCount.labels(jobFullName).inc();
      if (lastBuild.getResult() == Result.SUCCESS) {
        jobSuccessCount.labels(jobFullName).inc();
      } else {
        jobFailedCount.labels(jobFullName).inc();
      }
      lastBuild = lastBuild.getPreviousBuild();
    }
  }

  private boolean updateLastBuildMap(String jobFullName, Run lastBuild) {
    Run cacheLastBuild = cachedBuilds.getLastBuildByJobName(jobFullName);
    if (cacheLastBuild != null && lastBuild.getNumber() == cacheLastBuild.getNumber()) {
      return false;
    }
    cachedBuilds.putLastBuild(jobFullName, lastBuild);
    return true;
  }

  private void setRecoveryAndLeadTimeMetrics(String jobFullName, Run lastBuild) {
    Run failedBuild = cachedBuilds.getFailedJobByJobName(jobFullName);
    if (lastBuild.getResult() != Result.SUCCESS && failedBuild == null) {
      cachedBuilds.putFailedJob(jobFullName, lastBuild);
    }
    if (lastBuild.getResult() == Result.SUCCESS) {
      if (failedBuild != null) {
        logger.info("set recovery time [{}]", lastBuild.getDisplayName());
        jobRecoverTime.labels(jobFullName)
            .set(getJobEndTime(lastBuild) - getJobEndTime(failedBuild));
        jobLeadTime.labels(jobFullName).set(getJobEndTime(lastBuild) - failedBuild.getStartTimeInMillis());
        cachedBuilds.removeFailedJobByJobName(jobFullName);
      } else {
        jobLeadTime.labels(jobFullName).set(lastBuild.getDuration());
      }
    }
  }

  private long getJobEndTime(Run build) {
    return build.getStartTimeInMillis() + build.getDuration();
  }
}