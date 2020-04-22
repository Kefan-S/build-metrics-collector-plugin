package io.jenkins.plugins.collector.config;

import hudson.Extension;
import hudson.init.Initializer;
import hudson.model.AbstractItem;
import hudson.model.Descriptor;
import io.jenkins.plugins.collector.data.JobProvider;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import jenkins.YesNoMaybe;
import jenkins.model.GlobalConfiguration;
import jenkins.model.Jenkins;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.StaplerRequest;

import static hudson.init.InitMilestone.JOB_LOADED;

@Extension(dynamicLoadable = YesNoMaybe.NO)
public class PrometheusConfiguration extends GlobalConfiguration {

  static final long DEFAULT_COLLECTING_METRICS_PERIOD_IN_SECONDS = TimeUnit.SECONDS.toSeconds(120);

  private Long collectingMetricsPeriodInSeconds = null;
  private String jobName;
  private Map<String, Boolean> jobNameMap;
  private String collectedJob;

  public PrometheusConfiguration() {
    load();
    setCollectingMetricsPeriodInSeconds(collectingMetricsPeriodInSeconds);
    setJobName(jobName);
    setJobNameMap(jobNameMap);
    setCollectedJob(collectedJob);

  }

  @Initializer(after = JOB_LOADED)
  public void init() {
    final List<String> jobNames = new JobProvider(Jenkins.getInstance()).getAllJobs().stream()
        .map(AbstractItem::getFullName).collect(Collectors.toList());

    setJobName(String.join(",", jobNames));

    final Map<String, Boolean> collect = jobNames.stream().collect(Collectors.toMap(jobName -> jobName, jobName -> false));
    setJobNameMap(collect);
  }

  @Override
  public boolean configure(StaplerRequest req, JSONObject json) throws FormException {
    collectingMetricsPeriodInSeconds = validateProcessingMetricsPeriodInSeconds(json);
    jobName = validateProcessingJobName(json);
    jobNameMap = validateProcessingJobNameMap(json);
    collectedJob = validateCollectedJob(json);
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

  public void setJobName(String jobName) {
    this.jobName = jobName;
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

  private String validateProcessingJobName(JSONObject json) throws FormException {
    String value = json.getString("jobName");
    if (StringUtils.isNotEmpty(value)) {
      return value;
    }
    throw new FormException("jobName must not be empty", "jobName");
  }

  private Map<String, Boolean> validateProcessingJobNameMap(JSONObject json) {
    final JSONArray jobNameList = json.getJSONArray("jobNameMap");
    Map<String, Boolean> data = new LinkedHashMap<>();
    jobNameList.stream().forEach(jobName -> data.put(jobName.toString(), false));
    return data;
  }

  private String validateCollectedJob(JSONObject json) {
    return json.getString("collectedJob");
  }
}
