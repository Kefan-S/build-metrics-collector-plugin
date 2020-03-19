package io.jenkins.plugins.collector.config;

import hudson.Extension;
import hudson.model.Descriptor;
import jenkins.YesNoMaybe;
import jenkins.model.GlobalConfiguration;
import jenkins.model.Jenkins;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.StaplerRequest;

import java.util.concurrent.TimeUnit;

@Extension(dynamicLoadable = YesNoMaybe.NO)
public class PrometheusConfiguration extends GlobalConfiguration {

  static final long DEFAULT_COLLECTING_METRICS_PERIOD_IN_SECONDS = TimeUnit.SECONDS.toSeconds(15);

  private Long collectingMetricsPeriodInSeconds = null;

  public PrometheusConfiguration() {

    load();
    setCollectingMetricsPeriodInSeconds(collectingMetricsPeriodInSeconds);
  }

  @Override
  public boolean configure(StaplerRequest req, JSONObject json) throws FormException {
    collectingMetricsPeriodInSeconds = validateProcessingMetricsPeriodInSeconds(json);
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

  public void setCollectingMetricsPeriodInSeconds(Long collectingMetricsPeriodInSeconds) {
    if (collectingMetricsPeriodInSeconds == null) {
      this.collectingMetricsPeriodInSeconds = DEFAULT_COLLECTING_METRICS_PERIOD_IN_SECONDS;
    } else {
      this.collectingMetricsPeriodInSeconds = collectingMetricsPeriodInSeconds;
    }
    save();
  }

  private Long validateProcessingMetricsPeriodInSeconds(JSONObject json) throws FormException {
    try {
      long value = json.getLong("collectingMetricsPeriodInSeconds");
      if (value > 0) {
        return value;
      }
    } catch (JSONException ignored) {
    }
    throw new FormException("CollectingMetricsPeriodInSeconds must be a positive integer", "collectingMetricsPeriodInSeconds");
  }
}
