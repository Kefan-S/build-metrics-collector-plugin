package io.jenkins.plugins.collector.config;

import hudson.Extension;
import hudson.model.Descriptor;
import java.util.concurrent.TimeUnit;
import jenkins.YesNoMaybe;
import jenkins.model.GlobalConfiguration;
import jenkins.model.Jenkins;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.StaplerRequest;

@Extension(dynamicLoadable = YesNoMaybe.NO)
public class PrometheusConfiguration extends GlobalConfiguration {

  static final long DEFAULT_COLLECTING_METRICS_PERIOD_IN_SECONDS = TimeUnit.SECONDS.toSeconds(15);

  private Long collectingMetricsPeriodInSeconds = null;
  private String jobName;

  public PrometheusConfiguration() {
    load();
    setCollectingMetricsPeriodInSeconds(collectingMetricsPeriodInSeconds);
    setJobName(jobName);
  }

  @Override
  public boolean configure(StaplerRequest req, JSONObject json) throws FormException {
    collectingMetricsPeriodInSeconds = validateProcessingMetricsPeriodInSeconds(json);
    jobName = validateProcessingJobName(json);
    save();
    return super.configure(req, json);
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
    if (StringUtils.isEmpty(jobName)) {
      this.jobName = "build-metrics-collector-plugin";
    } else {
      this.jobName = jobName;
    }
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
    String value = json.getString("jobName");
    if (StringUtils.isNotEmpty(value)) {
      return value;
    }
    throw new FormException("jobName must not be empty", "jobName");
  }
}
