package io.jenkins.plugins.collector.consumer.prometheus;

import io.jenkins.plugins.collector.model.BuildInfo;
import io.prometheus.client.Collector.MetricFamilySamples;
import io.prometheus.client.Gauge;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import static io.jenkins.plugins.collector.config.Constant.METRICS_LABEL_NAME_ARRAY;
import static io.jenkins.plugins.collector.config.Constant.METRICS_NAMESPACE;
import static io.jenkins.plugins.collector.config.Constant.METRICS_NAME_PREFIX;
import static io.jenkins.plugins.collector.config.Constant.METRICS_SUBSYSTEM;
import static java.util.stream.Collectors.toList;

class PrometheusAdapter{

  List<MetricFamilySamples> adapt(BuildInfo buildInfo) {
    LinkedList<Gauge> gauges = new LinkedList<>();
    String[] metricsLabels = getMetricsLabels(buildInfo);

    bindGauge("_last_build_start_timestamp", "One build start timestamp", gauges, buildInfo.getStartTime(), metricsLabels);
    bindGauge("_last_build_duration_in_milliseconds", "One build duration in milliseconds", gauges, buildInfo.getDuration(), metricsLabels);
    bindGauge("_merge_lead_time", "Code Merge Lead Time in milliseconds", gauges, buildInfo.getLeadTime(), metricsLabels);
    bindGauge("_failed_build_recovery_time", "Failed Build Recovery Time in milliseconds", gauges, buildInfo.getRecoverTime(), metricsLabels);

    return gauges.stream()
        .map(Gauge::collect)
        .flatMap(Collection::stream)
        .collect(toList());
  }

  private String[] getMetricsLabels(BuildInfo buildInfo) {
    String jobFullName = buildInfo.getJenkinsJob();
    String trigger = buildInfo.getTriggeredBy();
    String resultValue = buildInfo.getResult();
    return new String[]{jobFullName, trigger, resultValue};
  }

  private void bindGauge(String nameSuffix, String description, List<Gauge> gauges, Long value, String... labels) {
    if (Objects.nonNull(value)) {
      Gauge gauge = Gauge.build()
          .name(METRICS_NAME_PREFIX + nameSuffix)
          .subsystem(METRICS_SUBSYSTEM).namespace(METRICS_NAMESPACE)
          .labelNames(METRICS_LABEL_NAME_ARRAY.toArray(new String[0]))
          .help(description)
          .create();
      gauge.labels(labels).set(value);
      gauges.add(gauge);
    }
  }
}
