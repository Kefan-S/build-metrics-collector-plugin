package io.jenkins.plugins.collector.adapter;

import io.jenkins.plugins.collector.model.BuildInfo;

public interface MetricsAdapter<T> {

  T adapt(BuildInfo buildInfo);
}
