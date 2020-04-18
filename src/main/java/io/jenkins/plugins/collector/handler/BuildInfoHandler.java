package io.jenkins.plugins.collector.handler;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import hudson.model.Run;
import io.prometheus.client.Collector.MetricFamilySamples;
import io.prometheus.client.Gauge;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;
import javax.annotation.Nonnull;

import static io.jenkins.plugins.collector.util.BuildUtil.getLabels;
import static java.util.stream.Collectors.toList;

public class BuildInfoHandler extends AbstractHandler implements Function<Run, List<MetricFamilySamples>> {

  private Gauge buildDurationMetrics;
  private Gauge buildStartTimeMetrics;

  @Inject
  public BuildInfoHandler(@Named("durationGauge") Gauge buildDurationMetrics,
                          @Named("startTimeGauge") Gauge buildStartTimeMetrics) {
    this.buildDurationMetrics = buildDurationMetrics;
    this.buildStartTimeMetrics = buildStartTimeMetrics;
  }

  @Override
  public List<MetricFamilySamples> apply(@Nonnull Run successBuild) {
    processMetrics(successBuild, null, buildDurationMetrics, buildStartTimeMetrics);
    return Stream.of(buildDurationMetrics, buildStartTimeMetrics)
        .map(Gauge::collect)
        .flatMap(Collection::stream)
        .collect(toList());
  }

  @Override
  void setMetricValue(Run build, Long metricValue) {
    buildDurationMetrics.labels(getLabels(build)).set(build.getDuration());
    buildStartTimeMetrics.labels(getLabels(build)).set(build.getStartTimeInMillis());
  }
}
