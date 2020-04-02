package io.jenkins.plugins.collector.actions.gauge;

import hudson.model.Run;
import io.jenkins.plugins.collector.util.CustomizeMetrics;
import io.prometheus.client.Gauge;
import java.util.function.BiConsumer;
import javax.annotation.Nonnull;

public class BuildInfoHandler implements BiConsumer<String[], Run> {

    CustomizeMetrics customizeMetrics;
    private Gauge buildDurationMetrics;
    private Gauge buildStartTimeMetrics;

    public BuildInfoHandler(CustomizeMetrics customizeMetrics, Gauge buildDurationMetrics, Gauge buildStartTimeMetrics) {
        this.customizeMetrics = customizeMetrics;
        this.buildDurationMetrics = buildDurationMetrics;
        this.buildStartTimeMetrics = buildStartTimeMetrics;
    }

    public void accept(@Nonnull String[] labels, @Nonnull Run build) {
        buildDurationMetrics.labels(labels).set(build.getDuration());
        buildStartTimeMetrics.labels(labels).set(build.getStartTimeInMillis());
        customizeMetrics.addCollector(buildDurationMetrics);
        customizeMetrics.addCollector(buildStartTimeMetrics);
    }
}
