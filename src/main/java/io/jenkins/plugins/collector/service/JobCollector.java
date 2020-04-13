package io.jenkins.plugins.collector.service;


import com.google.inject.Inject;
import hudson.model.Run;
import io.jenkins.plugins.collector.data.BuildProvider;
import io.jenkins.plugins.collector.data.CustomizeMetrics;
import io.prometheus.client.Collector;
import java.util.List;
import java.util.function.Consumer;

public class JobCollector extends Collector {

  private final Consumer<Run> buildHandler;

  private CustomizeMetrics customizeMetrics;
  private BuildProvider buildProvider;

  @Inject
  public JobCollector(CustomizeMetrics customizeMetrics,
                      Consumer<Run> buildHandler,
                      BuildProvider buildProvider) {
    this.customizeMetrics = customizeMetrics;
    this.buildHandler = buildHandler;
    this.buildProvider = buildProvider;
  }

  @Override
  public List<MetricFamilySamples> collect() {
    customizeMetrics.initMetrics();
    collectJob();
    return customizeMetrics.getMetricsList();
  }

  private void collectJob() {
    buildProvider.getNeedToHandleBuilds().forEach(
        run -> {
          buildHandler.accept(run);
          buildProvider.remove(run);
        }
    );
  }
}
