package io.jenkins.plugins.collector.service;


import com.google.inject.Inject;
import hudson.model.Run;
import io.jenkins.plugins.collector.data.BuildProvider;
import io.prometheus.client.Collector;
import io.prometheus.client.SimpleCollector;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

import static java.util.stream.Collectors.toList;

public class JobCollector extends Collector {

  private final List<Function<Run, List<SimpleCollector>>> buildHandler;

  private BuildProvider buildProvider;

  @Inject
  public JobCollector(List<Function<Run, List<SimpleCollector>>> buildHandler,
                      BuildProvider buildProvider) {
    this.buildHandler = buildHandler;
    this.buildProvider = buildProvider;
  }

  @Override
  public List<MetricFamilySamples> collect() {
    return collectJobMetric();
  }

  private List<MetricFamilySamples> collectJobMetric() {
    final List<SimpleCollector> collectors = getCollectors();

    collectors.forEach(SimpleCollector::clear);

    return getJobMetric(collectors);
  }

  private List<SimpleCollector> getCollectors() {
    return buildProvider.getNeedToHandleBuilds().stream()
        .map(this::getMetricsForBuild)
        .flatMap(Collection::stream)
        .collect(toList());
  }

  private List<SimpleCollector> getMetricsForBuild(Run build) {
    List<SimpleCollector> metrics = buildHandler.stream()
        .map(handler -> handler.apply(build))
        .flatMap(Collection::stream)
        .collect(toList());
    buildProvider.remove(build);
    return metrics;
  }

  private List<MetricFamilySamples> getJobMetric(List<SimpleCollector> collectors) {
    return collectors.stream()
        .map(Collector::collect)
        .flatMap(Collection::stream)
        .collect(toList());
  }
}
