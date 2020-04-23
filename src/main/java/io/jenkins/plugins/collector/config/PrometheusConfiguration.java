package io.jenkins.plugins.collector.config;

import com.google.gson.Gson;
import hudson.Extension;
import hudson.init.Initializer;
import hudson.model.AbstractItem;
import hudson.model.Descriptor;
import io.jenkins.plugins.collector.data.JobProvider;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import jenkins.YesNoMaybe;
import jenkins.model.GlobalConfiguration;
import jenkins.model.Jenkins;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.StaplerRequest;

import static hudson.init.InitMilestone.JOB_LOADED;

@Extension(dynamicLoadable = YesNoMaybe.NO)
public class PrometheusConfiguration extends GlobalConfiguration {

  static final long DEFAULT_COLLECTING_METRICS_PERIOD_IN_SECONDS = TimeUnit.SECONDS.toSeconds(120);

  private Long collectingMetricsPeriodInSeconds = null;
  private Map<String, Boolean> jobNameMap;
  private String collectedJob;

  public PrometheusConfiguration() {
    load();
    setCollectingMetricsPeriodInSeconds(collectingMetricsPeriodInSeconds);
    setJobNameMap(jobNameMap);
    setCollectedJob(collectedJob);
  }

  @Initializer(after = JOB_LOADED)
  public void init() {
    final Map<String, Boolean> collect = new JobProvider(Jenkins.getInstance()).getAllJobs().stream()
        .map(AbstractItem::getFullName)
        .collect(Collectors.toMap(jobName -> jobName, jobName -> true));
    setJobNameMap(collect);
  }

  @Override
  public boolean configure(StaplerRequest req, JSONObject json) throws FormException {
    collectingMetricsPeriodInSeconds = validateProcessingMetricsPeriodInSeconds(json);
    jobNameMap = validateProcessingJobNameMap(json);
    collectedJob = validateCollectedJob(json);
    save();
    return true;
  }

  public static PrometheusConfiguration get() {
    Descriptor configuration = Jenkins.getInstance().getDescriptor(PrometheusConfiguration.class);
    return (PrometheusConfiguration) configuration;
  }


  public long getCollectingMetricsPeriodInSeconds() {
    return collectingMetricsPeriodInSeconds;
  }

  public Map<String, Boolean> getJobNameMap() {
    return jobNameMap;
  }

  public String getCollectedJob() {
    return collectedJob;
  }

  public void setCollectingMetricsPeriodInSeconds(Long collectingMetricsPeriodInSeconds) {
    if (collectingMetricsPeriodInSeconds == null) {
      this.collectingMetricsPeriodInSeconds = DEFAULT_COLLECTING_METRICS_PERIOD_IN_SECONDS;
    } else {
      this.collectingMetricsPeriodInSeconds = collectingMetricsPeriodInSeconds;
    }
    save();
  }

  public void setJobNameMap(Map<String, Boolean> jobNameMap) {
    this.jobNameMap = jobNameMap;
    save();
  }

  public void setCollectedJob(String collectedJob) {
    this.collectedJob = collectedJob;
    save();
  }

  private Long validateProcessingMetricsPeriodInSeconds(JSONObject json) throws FormException {
    long value = json.getLong("collectingMetricsPeriodInSeconds");
    if (value > 0) {
      return value;
    }
    throw new FormException("CollectingMetricsPeriodInSeconds must be a positive integer", "collectingMetricsPeriodInSeconds");
  }

  private Map<String, Boolean> validateProcessingJobNameMap(JSONObject json) {
    final HashMap<String, Boolean> operatedJob = new Gson().fromJson(json.getString("collectedJob"), HashMap.class);
    for (Entry<String, Boolean> stringBooleanEntry : operatedJob.entrySet()) {
      this.jobNameMap.put(stringBooleanEntry.getKey(), stringBooleanEntry.getValue());
    }
    return this.jobNameMap;
  }

  private String validateCollectedJob(JSONObject json) {
    return json.getString("collectedJob");
  }
}
