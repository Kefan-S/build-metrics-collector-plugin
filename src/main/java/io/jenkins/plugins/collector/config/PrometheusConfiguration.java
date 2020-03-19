package io.jenkins.plugins.collector.config;

import hudson.Extension;
import hudson.model.Descriptor;
import io.jenkins.plugins.collector.service.PrometheusAsyncWorker;
import jenkins.YesNoMaybe;
import jenkins.model.GlobalConfiguration;
import jenkins.model.Jenkins;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.StaplerRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

@Extension(dynamicLoadable = YesNoMaybe.NO)
public class PrometheusConfiguration extends GlobalConfiguration {
  private static final Logger logger = LoggerFactory.getLogger(PrometheusAsyncWorker.class);

  static final long DEFAULT_COLLECTING_METRICS_PERIOD_IN_SECONDS = TimeUnit.SECONDS.toMillis(120);

  private Long collectingMetricsPeriodInSeconds = null;

  public PrometheusConfiguration() {

    load();
    logger.info("set collect time {} ", collectingMetricsPeriodInSeconds);
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
    logger.info("_____set collect time {} ", collectingMetricsPeriodInSeconds);
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
