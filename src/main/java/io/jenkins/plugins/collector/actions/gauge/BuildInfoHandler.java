package io.jenkins.plugins.collector.actions.gauge;

import hudson.model.Run;
import io.prometheus.client.Gauge;

import java.util.function.BiConsumer;

import static io.jenkins.plugins.collector.config.Constant.METRICS_LABEL_NAME_ARRAY;
import static io.jenkins.plugins.collector.config.Constant.METRICS_NAMESPACE;
import static io.jenkins.plugins.collector.config.Constant.METRICS_NAME_PREFIX;
import static io.jenkins.plugins.collector.config.Constant.METRICS_SUBSYSTEM;
import static io.jenkins.plugins.collector.util.CustomizeMetrics.addCollector;

public class BuildInfoHandler implements BiConsumer<String[], Run> {

    private Gauge buildDurationMetrics = Gauge.build()
            .name(METRICS_NAME_PREFIX + "_last_build_duration_in_milliseconds")
            .subsystem(METRICS_SUBSYSTEM).namespace(METRICS_NAMESPACE)
            .labelNames(METRICS_LABEL_NAME_ARRAY)
            .help("One build duration in milliseconds")
            .create();

    private Gauge buildStartTimeMetrics = Gauge.build()
            .name(METRICS_NAME_PREFIX + "_last_build_start_timestamp")
            .subsystem(METRICS_SUBSYSTEM).namespace(METRICS_NAMESPACE)
            .labelNames(METRICS_LABEL_NAME_ARRAY)
            .help("One build start timestamp")
            .create();

    public void accept(String[] labels, Run build) {
        buildDurationMetrics.labels(labels).set(build.getDuration());
        buildStartTimeMetrics.labels(labels).set(build.getStartTimeInMillis());
        addCollector(buildDurationMetrics);
        addCollector(buildStartTimeMetrics);
    }
}
