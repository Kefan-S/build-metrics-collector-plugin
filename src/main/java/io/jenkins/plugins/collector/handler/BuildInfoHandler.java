package io.jenkins.plugins.collector.handler;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import hudson.model.Run;
import io.prometheus.client.Collector.MetricFamilySamples;
import io.prometheus.client.Gauge;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import javax.annotation.Nonnull;

import static io.jenkins.plugins.collector.util.BuildUtil.getLabels;

public class BuildInfoHandler implements Function<Run, List<MetricFamilySamples>> {

  private Gauge buildDurationMetrics;
  private Gauge buildStartTimeMetrics;

  @Inject
  public BuildInfoHandler(@Named("durationGauge") Gauge buildDurationMetrics,
                          @Named("startTimeGauge") Gauge buildStartTimeMetrics) {
    this.buildDurationMetrics = buildDurationMetrics;
    this.buildStartTimeMetrics = buildStartTimeMetrics;
  }

  @Override
  public List<MetricFamilySamples> apply(@Nonnull Run build) {
    buildDurationMetrics.clear();
    buildStartTimeMetrics.clear();
    buildDurationMetrics.labels(getLabels(build)).set(build.getDuration());
    buildStartTimeMetrics.labels(getLabels(build)).set(build.getStartTimeInMillis());
    List<MetricFamilySamples> list = new ArrayList<>();
    list.addAll(buildDurationMetrics.collect());
    list.addAll(buildStartTimeMetrics.collect());
    return list;
  }

}
