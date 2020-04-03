package io.jenkins.plugins.collector.actions.gauge;

import hudson.model.Run;
import io.prometheus.client.Gauge;
import java.util.function.BiConsumer;
import javax.annotation.Nonnull;

public class BuildInfoHandler implements BiConsumer<String[], Run> {

  private Gauge buildDurationMetrics;
  private Gauge buildStartTimeMetrics;

  public BuildInfoHandler(Gauge buildDurationMetrics, Gauge buildStartTimeMetrics) {
    this.buildDurationMetrics = buildDurationMetrics;
    this.buildStartTimeMetrics = buildStartTimeMetrics;
  }

  @Override
  public void accept(@Nonnull String[] labels, @Nonnull Run build) {
    buildDurationMetrics.labels(labels).set(build.getDuration());
    buildStartTimeMetrics.labels(labels).set(build.getStartTimeInMillis());
  }
}
