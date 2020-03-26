package io.jenkins.plugins.collector;


import hudson.model.Result;
import hudson.model.Run;
import hudson.util.RunList;
import io.jenkins.plugins.collector.actions.BuildStatusCounter;
import io.jenkins.plugins.collector.util.CachedBuilds;
import io.jenkins.plugins.collector.util.Jobs;
import io.prometheus.client.Collector;
import io.prometheus.client.Gauge;
import io.reactivex.rxjava3.core.Observable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static io.jenkins.plugins.collector.config.Constant.FULLNAME;
import static io.jenkins.plugins.collector.config.Constant.LABEL_NAME_ARRAY;
import static io.jenkins.plugins.collector.config.Constant.NAMESPACE;
import static io.jenkins.plugins.collector.config.Constant.SUBSYSTEM;

public class JobCollector extends Collector {

  private static final Logger logger = LoggerFactory.getLogger(JobCollector.class);


  private Gauge jobDuration;
  private Gauge jobResult;
  private Gauge jobStartTime;
  private Gauge jobRecoverTime;
  private Gauge jobLeadTime;
  private CachedBuilds cachedBuilds = new CachedBuilds();

  @Override
  public List<MetricFamilySamples> collect() {

    List<MetricFamilySamples> samples = new ArrayList<>();

    jobDuration = Gauge.build()
        .name(FULLNAME + "_last_build_duration_in_milliseconds")
        .subsystem(SUBSYSTEM).namespace(NAMESPACE)
        .labelNames(LABEL_NAME_ARRAY)
        .help("Last Build times in milliseconds")
        .create();

    jobStartTime = Gauge.build()
        .name(FULLNAME + "_last_build_start_time_in_milliseconds")
        .subsystem(SUBSYSTEM).namespace(NAMESPACE)
        .labelNames(LABEL_NAME_ARRAY)
        .help("Last Build Start Time in milliseconds")
        .create();

    jobResult = Gauge.build()
        .name(FULLNAME + "_last_build_result_code")
        .subsystem(SUBSYSTEM).namespace(NAMESPACE)
        .labelNames(LABEL_NAME_ARRAY)
        .help("Last Build Result")
        .create();

    jobRecoverTime = Gauge.build()
        .name(FULLNAME + "_failed_build_recovery_time")
        .subsystem(SUBSYSTEM).namespace(NAMESPACE)
        .labelNames(LABEL_NAME_ARRAY)
        .help("Failed Build Recovery Time in milliseconds")
        .create();

    jobLeadTime = Gauge.build()
        .name(FULLNAME + "_merge_lead_time")
        .subsystem(SUBSYSTEM).namespace(NAMESPACE)
        .labelNames(LABEL_NAME_ARRAY)
        .help("Code Merge Lead Time in milliseconds")
        .create();

    Jobs.forEachJob(job -> {

      logger.debug("Determining if we are already appending metrics for job [{}]", job.getName());

      String jobFullName = job.getFullName();

      if (!job.isBuildable()) {
        logger.debug("job [{}] is disabled", jobFullName);
        return;
      }

      logger.debug("Job [{}] is not already added. Appending its metrics", job.getName());

      long end = Instant.now().toEpochMilli();

      ((RunList<Run>) job.getBuilds().byTimestamp(end - 15000, end)).forEach((Run run) -> {
        System.out.println("Find the build: "+run.getFullDisplayName());
        Observable.interval(1000, TimeUnit.MILLISECONDS)
                .map(signal -> run)
                .filter(build -> Objects.nonNull(build.getResult()))
                .take(1)
                .subscribe(BuildStatusCounter.getInstance());
      });

      Run lastBuild = job.getLastBuild();
      if (lastBuild != null && lastBuild.getResult() != null) {
        setBuildMetricsForLastBuild(lastBuild, jobFullName);
      }
    });

    samples.addAll(jobDuration.collect());
    samples.addAll(jobStartTime.collect());
    samples.addAll(jobResult.collect());
    samples.addAll(jobRecoverTime.collect());
    samples.addAll(jobLeadTime.collect());
    samples.addAll(BuildStatusCounter.getInstance().getMetricsList());
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
