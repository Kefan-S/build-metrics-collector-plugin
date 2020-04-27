package io.jenkins.plugins.collector.service;

import io.jenkins.plugins.collector.model.Metrics;
import java.util.List;
import java.util.function.Consumer;

public interface PrometheusMetrics extends Consumer<List<Metrics>> {

  String getMetrics();

}
