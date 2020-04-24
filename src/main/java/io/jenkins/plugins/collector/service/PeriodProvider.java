package io.jenkins.plugins.collector.service;

import hudson.Extension;
import io.jenkins.plugins.collector.config.PrometheusConfiguration;
import java.util.Objects;
import java.util.Optional;
import jenkins.model.Jenkins;

@Extension
public class PeriodProvider {

  private long previousPeriodInSeconds;
  private long currentPeriodInSeconds;

  public PeriodProvider() {
    this(PrometheusConfiguration.get().getCollectingMetricsPeriodInSeconds(), PrometheusConfiguration.get().getCollectingMetricsPeriodInSeconds());
  }

  public PeriodProvider(long previousPeriodInSeconds, long currentPeriodInSeconds) {
    this.previousPeriodInSeconds = previousPeriodInSeconds;
    this.currentPeriodInSeconds = currentPeriodInSeconds;
  }

  public static PeriodProvider get() {
    return Optional.ofNullable(Jenkins.getInstanceOrNull())
        .map(j -> j.getExtensionList(PeriodProvider.class).get(0))
        .orElse(null);
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
  public boolean equals(Object anotherObject) {
    if (this == anotherObject) {
      return true;
    }

    if (anotherObject instanceof PeriodProvider) {
      PeriodProvider anotherPeriodProvider = (PeriodProvider) anotherObject;
      return this.currentPeriodInSeconds == anotherPeriodProvider.currentPeriodInSeconds && this.previousPeriodInSeconds == anotherPeriodProvider.previousPeriodInSeconds;
    }

    return false;
  }

  @Override
  public int hashCode() {
    return Objects.hash(previousPeriodInSeconds, currentPeriodInSeconds);
  }
}
