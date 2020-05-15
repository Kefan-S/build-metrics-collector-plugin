package io.jenkins.plugins.collector.consumer.prometheus;

import io.jenkins.plugins.collector.model.BuildInfo;
import java.util.List;
import java.util.function.Consumer;

public interface PrometheusMetrics extends Consumer<List<BuildInfo>> {

  String getMetrics();

}
