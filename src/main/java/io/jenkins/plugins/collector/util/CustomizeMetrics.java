package io.jenkins.plugins.collector.util;

import io.prometheus.client.Collector;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class CustomizeMetrics {

    private final List<Collector> collectors = new LinkedList<>();

    public void initMetrics() {
        collectors.clear();
    }

    public void addCollector(Collector collector) {
        collectors.add(collector);
    }

    public List<Collector.MetricFamilySamples> getMetricsList() {
        return  collectors
                .stream()
                .map(Collector::collect)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }
}
