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
    public Gauge ONE_BUILD_RESULT_FROM_LAST_STATISTICAL_PERIOD = Gauge.build()
            .name(METRICS_NAME_PREFIX + "_last_build_result_code")
            .subsystem(METRICS_SUBSYSTEM).namespace(METRICS_NAMESPACE)
            .labelNames(METRICS_LABEL_NAME_ARRAY)
            .help("One build result")
            .create();

    public Gauge ONE_BUILD_DURATION_FROM_LAST_STATISTICAL_PERIOD = Gauge.build()
            .name(METRICS_NAME_PREFIX + "_last_build_duration_in_milliseconds")
            .subsystem(METRICS_SUBSYSTEM).namespace(METRICS_NAMESPACE)
            .labelNames(METRICS_LABEL_NAME_ARRAY)
            .help("One build duration in milliseconds")
            .create();

    public Gauge ONE_BUILD_STARTTIME_FROM_LAST_STATISTICAL_PERIOD = Gauge.build()
            .name(METRICS_NAME_PREFIX + "_last_build_start_timestamp")
            .subsystem(METRICS_SUBSYSTEM).namespace(METRICS_NAMESPACE)
            .labelNames(METRICS_LABEL_NAME_ARRAY)
            .help("One build start timestamp")
            .create();

    public void accept(String[] labels, Run build) {
        ONE_BUILD_DURATION_FROM_LAST_STATISTICAL_PERIOD.labels(labels).set(build.getDuration());
        ONE_BUILD_RESULT_FROM_LAST_STATISTICAL_PERIOD.labels(labels).set(build.getResult().ordinal);
        ONE_BUILD_STARTTIME_FROM_LAST_STATISTICAL_PERIOD.labels(labels).set(build.getStartTimeInMillis());
        addCollector(ONE_BUILD_RESULT_FROM_LAST_STATISTICAL_PERIOD);
        addCollector(ONE_BUILD_DURATION_FROM_LAST_STATISTICAL_PERIOD);
        addCollector(ONE_BUILD_STARTTIME_FROM_LAST_STATISTICAL_PERIOD);
    }
}
