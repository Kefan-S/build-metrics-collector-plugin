package io.jenkins.plugins.collector.data;

import com.google.inject.Inject;
import hudson.model.Job;
import hudson.model.Run;
import io.jenkins.plugins.collector.config.PrometheusConfiguration;
import io.jenkins.plugins.collector.exception.NoSuchBuildException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class BuildProvider {

  private Map<String, List<Run>> jobFullNameToUnhandledBuildsMap = new HashMap<>();
  private JobProvider jobProvider;
  private PrometheusConfiguration prometheusConfiguration;

  @Inject
  public BuildProvider(JobProvider jobProvider, PrometheusConfiguration prometheusConfiguration) {
    this.jobProvider = jobProvider;
    this.prometheusConfiguration = prometheusConfiguration;
  }

  public List<Run> getNeedToHandleBuilds() {
    updateUnhandledBuilds();
    return getAllNeedToHandleBuilds();
  }

  public void remove(Run run) {
    String jobFullName = run.getParent().getFullName();
    if (!jobFullNameToUnhandledBuildsMap.containsKey(jobFullName)) {
      throw new NoSuchBuildException(String.format("No Such Build: %s", run.getFullDisplayName()));
    }
    jobFullNameToUnhandledBuildsMap.get(jobFullName).remove(run);
  }

  private List<Run> getAllNeedToHandleBuilds() {
    return jobFullNameToUnhandledBuildsMap.values().stream()
        .map(this::getFirstCompletedBuild)
        .filter(Objects::nonNull)
        .collect(Collectors.toList());
  }

  private Run getFirstCompletedBuild(List<Run> runs) {
    return runs.stream().filter(run -> !run.isBuilding()).findFirst().orElse(null);
  }

  private void updateUnhandledBuilds() {
    jobProvider.getAllJobs().stream().filter(job -> job.getFullName().equals(prometheusConfiguration.getJobName())).forEach(this::updateUnhandledBuildsByJob);
  }

  private void updateUnhandledBuildsByJob(Job job) {
    long end = Instant.now().toEpochMilli();
    String jobFullName = job.getFullName();
    List<Run> unHandledRuns = jobFullNameToUnhandledBuildsMap.getOrDefault(jobFullName, new ArrayList<>());
    long period = TimeUnit.SECONDS.toMillis(prometheusConfiguration.getCollectingMetricsPeriodInSeconds());
    unHandledRuns.addAll(job.getBuilds().byTimestamp(end - period, end));
    jobFullNameToUnhandledBuildsMap.put(jobFullName, unHandledRuns);
  }

}
