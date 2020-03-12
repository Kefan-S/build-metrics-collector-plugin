package io.jenkins.plugins.collector;


import hudson.model.Job;
import hudson.model.Run;
import hudson.util.RunList;
import io.jenkins.plugins.collector.util.Jobs;
import io.prometheus.client.Collector;
import io.prometheus.client.Counter;
import io.prometheus.client.Gauge;
import io.prometheus.client.Summary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class JobCollector extends Collector {

  private static final Logger logger = LoggerFactory.getLogger(JobCollector.class);

  private Counter jobCount;
  private Counter jobFailedCount;
  private Gauge jobDuration;
  private Summary jobTime;


  @Override
  public List<MetricFamilySamples> collect() {

    String namespace = "default";
    List<MetricFamilySamples> samples = new ArrayList<>();
    List<Job> jobs = new ArrayList<>();
    String fullname = "builds";
    String subsystem = "jenkins";
    String[] labelNameArray = {"jenkins_job", "start_time", "result", "build_id"};

    jobFailedCount = Counter.build()
        .name(fullname + "_failed_build_count")
        .subsystem(subsystem).namespace(namespace)
        .labelNames(labelNameArray)
        .help("Failed build count")
        .create();

    jobTime = Summary.build()
        .name(fullname + "_time_summary")
        .subsystem(subsystem).namespace(namespace)
        .labelNames(labelNameArray)
        .help("Time summary")
        .create();

    jobDuration = Gauge.build()
        .name(fullname + "_last_build_duration_milliseconds")
        .subsystem(subsystem).namespace(namespace)
        .labelNames(labelNameArray)
        .help("Build times in milliseconds of last build")
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

      RunList<Run> jobBuilds = job.getBuilds();
//      for (Run build : jobBuilds) {
//        b
//      }
      jobBuilds.forEach(build -> {
        if ( build!= null) {
          jobDuration.labels(job.getFullName(), build.getResult().toString(),
              String.valueOf(build.getStartTimeInMillis()), String.valueOf(build.getNumber())).set(build.getDuration());
        }
      });
    });

//    samples.addAll(jobFailedCount.collect());
    samples.addAll(jobDuration.collect());
//    samples.addAll(jobTime.collect());
    return samples;
  }
}
