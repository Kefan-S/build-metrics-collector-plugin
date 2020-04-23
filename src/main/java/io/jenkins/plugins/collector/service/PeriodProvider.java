package io.jenkins.plugins.collector.service;

import hudson.Extension;
import io.jenkins.plugins.collector.config.PrometheusConfiguration;
import jenkins.model.Jenkins;

@Extension
public class PeriodProvider {

  private long previousPeriodInSeconds;
  private long currentPeriodInSeconds;

  public PeriodProvider() {
    this.previousPeriodInSeconds = PrometheusConfiguration.get().getCollectingMetricsPeriodInSeconds();
    this.currentPeriodInSeconds = PrometheusConfiguration.get().getCollectingMetricsPeriodInSeconds();
  }

  public static PeriodProvider get() {
    return Jenkins.getInstanceOrNull().getExtensionList(PeriodProvider.class).get(0);
  }

  public long getPeriodInSeconds() {
    if (previousPeriodInSeconds == currentPeriodInSeconds) {
      return currentPeriodInSeconds;
    }

    long period = previousPeriodInSeconds + currentPeriodInSeconds;
    this.previousPeriodInSeconds = currentPeriodInSeconds;
    return period;
  }

  public void updatePeriods() {
    this.previousPeriodInSeconds = this.currentPeriodInSeconds;
    this.currentPeriodInSeconds = PrometheusConfiguration.get().getCollectingMetricsPeriodInSeconds();
  }
}
