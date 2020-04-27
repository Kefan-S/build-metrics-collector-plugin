package io.jenkins.plugins.collector.service;

import io.jenkins.plugins.collector.model.Metrics;
import java.util.ArrayList;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;

public class DefaultPrometheusMetricsTest {

  @Test
  public void should_get_metrics_which_be_job_collector_collected() {
    Metrics metrics = Metrics.builder()
        .leadTime(1L)
        .duration(2L)
        .startTime(3L)
        .triggeredBy("UnkownUser")
        .result("0")
        .jenkinsJob("fakePipline")
        .build();
    List<Metrics> metricsList = new ArrayList<>();
    metricsList.add(metrics);

    DefaultPrometheusMetrics defaultPrometheusMetrics = new DefaultPrometheusMetrics();
    defaultPrometheusMetrics.accept(metricsList);

    String expectedMetrics = "# HELP default_jenkins_builds_last_build_start_timestamp One build start timestamp\n" +
        "# TYPE default_jenkins_builds_last_build_start_timestamp gauge\n" +
        "default_jenkins_builds_last_build_start_timestamp{jenkinsJob=\"fakePipline\",triggeredBy=\"UnkownUser\",result=\"0\",} 3.0\n" +
        "# HELP default_jenkins_builds_last_build_duration_in_milliseconds One build duration in milliseconds\n" +
        "# TYPE default_jenkins_builds_last_build_duration_in_milliseconds gauge\n" +
        "default_jenkins_builds_last_build_duration_in_milliseconds{jenkinsJob=\"fakePipline\",triggeredBy=\"UnkownUser\",result=\"0\",} 2.0\n" +
        "# HELP default_jenkins_builds_merge_lead_time Code Merge Lead Time in milliseconds\n" +
        "# TYPE default_jenkins_builds_merge_lead_time gauge\n" +
        "default_jenkins_builds_merge_lead_time{jenkinsJob=\"fakePipline\",triggeredBy=\"UnkownUser\",result=\"0\",} 1.0\n";
    Assert.assertEquals(expectedMetrics, defaultPrometheusMetrics.getMetrics());

  }
}
