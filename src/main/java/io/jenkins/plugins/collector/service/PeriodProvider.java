package io.jenkins.plugins.collector.service;

import hudson.Extension;
import io.jenkins.plugins.collector.config.PrometheusConfiguration;
import java.util.Objects;
import jenkins.model.Jenkins;

@Extension
public class PeriodProvider {

  private long previousPeriodInSeconds;
  private long currentPeriodInSeconds;

  public PeriodProvider() {
    long periodInSeconds = PrometheusConfiguration.get().getCollectingMetricsPeriodInSeconds();
    new PeriodProvider(periodInSeconds, periodInSeconds);
  }

  public PeriodProvider(long previousPeriodInSeconds, long currentPeriodInSeconds) {
    this.previousPeriodInSeconds = previousPeriodInSeconds;
    this.currentPeriodInSeconds = currentPeriodInSeconds;
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

  @Override
  public boolean equals(Object obj) {
    if (Objects.isNull(obj) || obj.getClass() != PeriodProvider.class) {
      return false;
    }
    PeriodProvider o = (PeriodProvider) obj;
    return this.currentPeriodInSeconds == o.currentPeriodInSeconds && this.previousPeriodInSeconds == o.previousPeriodInSeconds;
  }
}
