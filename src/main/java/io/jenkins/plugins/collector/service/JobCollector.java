package io.jenkins.plugins.collector.service;


import com.google.inject.Inject;
import com.google.inject.name.Named;
import hudson.model.Run;
import io.jenkins.plugins.collector.data.BuildProvider;
import io.prometheus.client.Collector;
import io.prometheus.client.Gauge;
import io.prometheus.client.SimpleCollector;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.collections.CollectionUtils.isEmpty;

public class JobCollector extends Collector {

  private final List<Function<Run, List<SimpleCollector>>> buildHandler;

  private BuildProvider buildProvider;

  @Inject
  @Named("durationGauge")
  private Gauge durationGaugeMetric;

  @Inject
  @Named("leadTimeGauge")
  private Gauge leadTimeGaugeMetric;

  @Inject
  @Named("recoverTimeGauge")
  private Gauge recoverTimeGaugeMetric;

  @Inject
  @Named("startTimeGauge")
  private Gauge startTimeGaugeMetric;

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
    return buildProvider.getNeedToHandleBuilds()
          .stream()
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
    if (isEmpty(collectors)) {
      return newArrayList(durationGaugeMetric, leadTimeGaugeMetric, recoverTimeGaugeMetric, startTimeGaugeMetric).stream()
          .map(Collector::collect)
          .flatMap(Collection::stream)
          .collect(toList());
    }
    return collectors.stream()
        .map(Collector::collect)
        .flatMap(Collection::stream)
        .collect(toList());
  }
}
