package io.jenkins.plugins.collector.service;


import com.google.inject.Inject;
import hudson.model.Run;
import io.jenkins.plugins.collector.data.BuildProvider;
import io.prometheus.client.Collector;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

import static java.util.stream.Collectors.toList;

public class JobCollector extends Collector {

  private final List<Function<Run, List<MetricFamilySamples>>> buildHandler;

  private BuildProvider buildProvider;

  @Inject
  public JobCollector(List<Function<Run, List<MetricFamilySamples>>> buildHandler,
                      BuildProvider buildProvider) {
    this.buildHandler = buildHandler;
    this.buildProvider = buildProvider;
  }

  @Override
  public List<MetricFamilySamples> collect() {
    return buildProvider.getNeedToHandleBuilds().stream()
        .map(this::getMetricsForBuild)
        .flatMap(Collection::stream)
        .collect(toList());
  }

  private List<MetricFamilySamples> getMetricsForBuild(Run build) {
    List<MetricFamilySamples> metrics = buildHandler.stream()
        .map(handler -> handler.apply(build))
        .flatMap(Collection::stream)
        .collect(toList());
    buildProvider.remove(build);
    return metrics;
  }
}
