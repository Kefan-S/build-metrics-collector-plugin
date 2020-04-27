package io.jenkins.plugins.collector.adapter;

import io.jenkins.plugins.collector.model.Metrics;
import io.prometheus.client.Collector.MetricFamilySamples;
import io.prometheus.client.Gauge;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import static io.jenkins.plugins.collector.config.Constant.METRICS_LABEL_NAME_ARRAY;
import static io.jenkins.plugins.collector.config.Constant.METRICS_NAMESPACE;
import static io.jenkins.plugins.collector.config.Constant.METRICS_NAME_PREFIX;
import static io.jenkins.plugins.collector.config.Constant.METRICS_SUBSYSTEM;
import static java.util.stream.Collectors.toList;

public class PrometheusAdapter implements MetricsAdapter<List<MetricFamilySamples>>{

  @Override
  public List<MetricFamilySamples> adapt(Metrics metrics) {
    Gauge startTimeGauge = bindGauge("startTimeGauge", "_last_build_start_timestamp", "One build start timestamp");
    Gauge durationGauge = bindGauge("durationGauge", "_last_build_duration_in_milliseconds", "One build duration in milliseconds");

    String[] metricsLabels = getMetricsLabels(metrics);

    startTimeGauge.labels(metricsLabels).set(metrics.getStartTime());
    durationGauge.labels(metricsLabels).set(metrics.getDuration());

    ArrayList<Gauge> gauges = new ArrayList<>();
    gauges.add(startTimeGauge);
    gauges.add(durationGauge);
    if (Objects.nonNull(metrics.getLeadTime())) {
      Gauge leadTimeGauge = bindGauge("leadTimeGauge", "_merge_lead_time", "Code Merge Lead Time in milliseconds");
      leadTimeGauge.labels(metricsLabels).set(metrics.getLeadTime());
      gauges.add(leadTimeGauge);
    }

    if (Objects.nonNull(metrics.getRecoverTime())) {
      Gauge recoverTimeGauge =  bindGauge("recoverTimeGauge", "_failed_build_recovery_time", "Failed Build Recovery Time in milliseconds");
      recoverTimeGauge.labels(metricsLabels).set(metrics.getRecoverTime());
      gauges.add(recoverTimeGauge);
    }
    return gauges.stream()
        .map(Gauge::collect)
        .flatMap(Collection::stream)
        .collect(toList());
  }

  private String[] getMetricsLabels(Metrics metrics) {
    String jobFullName = metrics.getJenkinsJob();
    String trigger = metrics.getTriggeredBy();
    String resultValue = metrics.getResult();
    return new String[]{jobFullName, trigger, resultValue};
  }

  private Gauge bindGauge(String name, String nameSuffix, String description) {
    return Gauge.build()
        .name(METRICS_NAME_PREFIX + nameSuffix)
        .subsystem(METRICS_SUBSYSTEM).namespace(METRICS_NAMESPACE)
        .labelNames(METRICS_LABEL_NAME_ARRAY.toArray(new String[0]))
        .help(description)
        .create();
  }
}
