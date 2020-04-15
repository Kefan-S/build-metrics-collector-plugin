package io.jenkins.plugins.collector.handler;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import hudson.model.Run;
import io.prometheus.client.Gauge;
import io.prometheus.client.SimpleCollector;
import java.util.List;
import java.util.function.Function;
import javax.annotation.Nonnull;

import static com.google.common.collect.Lists.newArrayList;
import static io.jenkins.plugins.collector.util.BuildUtil.getLabels;

public class BuildInfoHandler implements Function<Run, List<SimpleCollector>> {

  private Gauge buildDurationMetrics;
  private Gauge buildStartTimeMetrics;

  @Inject
  public BuildInfoHandler(@Named("durationGauge") Gauge buildDurationMetrics,
                          @Named("startTimeGauge") Gauge buildStartTimeMetrics) {
    this.buildDurationMetrics = buildDurationMetrics;
    this.buildStartTimeMetrics = buildStartTimeMetrics;
  }

  @Override
  public List<SimpleCollector> apply(@Nonnull Run build) {
    buildDurationMetrics.labels(getLabels(build)).set(build.getDuration());
    buildStartTimeMetrics.labels(getLabels(build)).set(build.getStartTimeInMillis());
    return newArrayList(buildDurationMetrics, buildStartTimeMetrics);
  }

}
