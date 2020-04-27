package io.jenkins.plugins.collector.adapter;

import io.jenkins.plugins.collector.model.Metrics;

public interface MetricsAdapter<T> {

  T adapt(Metrics metrics);
}
