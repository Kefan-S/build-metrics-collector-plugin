package io.jenkins.plugins.collector.util;

import io.prometheus.client.Collector;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class CustomizeMetrics {

    private static final List<Collector> collectors = new LinkedList<>();

    public static void initMetrics() {
        collectors.clear();
    }

    public static void addCollector(Collector collector) {
        collectors.add(collector);
    }

    public static List<Collector.MetricFamilySamples> getMetricsList() {
        return  collectors
                .stream()
                .map(Collector::collect)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }
}
