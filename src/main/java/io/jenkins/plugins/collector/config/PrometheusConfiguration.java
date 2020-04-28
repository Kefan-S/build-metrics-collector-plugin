package io.jenkins.plugins.collector.config;

import hudson.Extension;
import hudson.model.Descriptor;
import io.jenkins.plugins.collector.service.AsyncWorkerManager;
import io.jenkins.plugins.collector.service.PeriodProvider;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import jenkins.YesNoMaybe;
import jenkins.model.GlobalConfiguration;
import jenkins.model.Jenkins;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.StaplerRequest;

@Extension(dynamicLoadable = YesNoMaybe.NO)
public class PrometheusConfiguration extends GlobalConfiguration {

  private static final long DEFAULT_COLLECTING_METRICS_PERIOD_IN_SECONDS = TimeUnit.SECONDS.toSeconds(15);
  private static final String REGEX_JOB_NAMES = "([^:]+(:[^:]+)*)*";

  private Long collectingMetricsPeriodInSeconds = null;
  private String jobName = "";

  public PrometheusConfiguration() {
    load();
    setCollectingMetricsPeriodInSeconds(collectingMetricsPeriodInSeconds);
    setJobName(jobName);
  }

  @Override
  public boolean configure(StaplerRequest req, JSONObject json) throws FormException {
    long previousCollectingMetricsPeriodInSeconds = collectingMetricsPeriodInSeconds.longValue();
    collectingMetricsPeriodInSeconds = validateProcessingMetricsPeriodInSeconds(json);
    jobName = validateProcessingJobName(json);
    save();
    boolean result = super.configure(req, json);
    if (collectingMetricsPeriodInSeconds != previousCollectingMetricsPeriodInSeconds) {
      Optional.ofNullable(AsyncWorkerManager.get()).ifPresent(AsyncWorkerManager::updateAsyncWorker);
      Optional.ofNullable(PeriodProvider.get()).ifPresent(PeriodProvider::updatePeriods);
    }
    return result;
  }

  public static PrometheusConfiguration get() {
    Descriptor configuration = Jenkins.getInstance().getDescriptor(PrometheusConfiguration.class);
    return (PrometheusConfiguration) configuration;
  }


  public long getCollectingMetricsPeriodInSeconds() {
    return collectingMetricsPeriodInSeconds;
  }

  public String getJobName() {
    return jobName;
  }

  public void setCollectingMetricsPeriodInSeconds(Long collectingMetricsPeriodInSeconds) {
    if (collectingMetricsPeriodInSeconds == null) {
      this.collectingMetricsPeriodInSeconds = DEFAULT_COLLECTING_METRICS_PERIOD_IN_SECONDS;
    } else {
      this.collectingMetricsPeriodInSeconds = collectingMetricsPeriodInSeconds;
    }
    save();
  }

  public void setJobName(String jobName) {
    this.jobName = jobName;
    save();
  }

  private Long validateProcessingMetricsPeriodInSeconds(JSONObject json) throws FormException {
    long value = json.getLong("collectingMetricsPeriodInSeconds");
    if (value > 0) {
      return value;
    }
    throw new FormException("CollectingMetricsPeriodInSeconds must be a positive integer", "collectingMetricsPeriodInSeconds");
  }

  private String validateProcessingJobName(JSONObject json) throws FormException {
    String jobName = json.getString("jobName");
    if (Pattern.matches(REGEX_JOB_NAMES, jobName)) {
      return jobName;
    }
    throw new FormException("the format of jobName you input must Conform to the correct format，like aa:bb:cc", "jobName");
  }
}
