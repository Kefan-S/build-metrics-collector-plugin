package io.jenkins.plugins.collector;


import hudson.model.Job;
import hudson.model.Run;
import io.jenkins.plugins.collector.util.Jobs;
import io.prometheus.client.Collector;
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


  @Override
  public List<MetricFamilySamples> collect() {

    String namespace = "default";
    List<MetricFamilySamples> samples = new ArrayList<>();
    List<Job> jobs = new ArrayList<>();
    String fullname = "builds";
    String subsystem = "jenkins";
    String[] labelNameArray = {"jenkins_job"};


    jobDuration = Gauge.build()
        .name(fullname + "_last_build_duration_milliseconds")
        .subsystem(subsystem).namespace(namespace)
        .labelNames(labelNameArray)
        .help("Last Build times in milliseconds")
        .create();

    jobStartTime = Gauge.build()
        .name(fullname + "_last_build_start_time_milliseconds")
        .subsystem(subsystem).namespace(namespace)
        .labelNames(labelNameArray)
        .help("Last Build Start Time in milliseconds")
        .create();

    jobResult = Gauge.build()
        .name(fullname + "_last_build_result")
        .subsystem(subsystem).namespace(namespace)
        .labelNames(labelNameArray)
        .help("Last Build Result")
        .create();

    jobRecoverTime = Gauge.build()
        .name(fullname + "_failed_build_recover_time")
        .subsystem(subsystem).namespace(namespace)
        .labelNames(labelNameArray)
        .help("Failed Build Recover Time in milliseconds")
        .create();

    Jobs.forEachJob(job -> {
      logger.debug("Determining if we are already appending metrics for job [{}]", job.getName());

      if (!job.isBuildable()) {
        logger.debug("job [{}] is disabled", job.getFullName());
        return;
      }

      for (Job old : jobs) {
        if (old.getFullName().equals(job.getFullName())) {
          // already added
          logger.debug("Job [{}] is already added", job.getName());
          return;
        }
      }
      logger.debug("Job [{}] is not already added. Appending its metrics", job.getName());
      jobs.add(job);

      Run lastBuild = job.getLastBuild();
      if (lastBuild != null) {
        jobDuration
            .labels(job.getFullName())
            .set(lastBuild.getDuration());
        jobStartTime
            .labels(job.getFullName())
            .set(lastBuild.getStartTimeInMillis());
        jobResult
            .labels(job.getFullName())
            .set(lastBuild.getResult().ordinal < 1 ? 1 : 0);
      }

    });

    samples.addAll(jobDuration.collect());
    samples.addAll(jobStartTime.collect());
    samples.addAll(jobResult.collect());
    return samples;
  }
}
