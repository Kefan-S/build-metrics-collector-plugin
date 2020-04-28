package io.jenkins.plugins.collector.adapter;

import io.jenkins.plugins.collector.model.BuildInfo;
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
  public List<MetricFamilySamples> adapt(BuildInfo buildInfo) {
    Gauge startTimeGauge = bindGauge("_last_build_start_timestamp", "One build start timestamp");
    Gauge durationGauge = bindGauge("_last_build_duration_in_milliseconds", "One build duration in milliseconds");

    String[] metricsLabels = getMetricsLabels(buildInfo);

    startTimeGauge.labels(metricsLabels).set(buildInfo.getStartTime());
    durationGauge.labels(metricsLabels).set(buildInfo.getDuration());

    ArrayList<Gauge> gauges = new ArrayList<>();
    gauges.add(startTimeGauge);
    gauges.add(durationGauge);
    if (Objects.nonNull(buildInfo.getLeadTime())) {
      Gauge leadTimeGauge = bindGauge("_merge_lead_time", "Code Merge Lead Time in milliseconds");
      leadTimeGauge.labels(metricsLabels).set(buildInfo.getLeadTime());
      gauges.add(leadTimeGauge);
    }

    if (Objects.nonNull(buildInfo.getRecoverTime())) {
      Gauge recoverTimeGauge =  bindGauge("_failed_build_recovery_time", "Failed Build Recovery Time in milliseconds");
      recoverTimeGauge.labels(metricsLabels).set(buildInfo.getRecoverTime());
      gauges.add(recoverTimeGauge);
    }
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

  private Gauge bindGauge(String nameSuffix, String description) {
    return Gauge.build()
        .name(METRICS_NAME_PREFIX + nameSuffix)
        .subsystem(METRICS_SUBSYSTEM).namespace(METRICS_NAMESPACE)
        .labelNames(METRICS_LABEL_NAME_ARRAY.toArray(new String[0]))
        .help(description)
        .create();
  }
}
