package io.jenkins.plugins.collector.service;

public interface PrometheusMetrics {

  String getMetrics();

  void collectMetrics();

}
