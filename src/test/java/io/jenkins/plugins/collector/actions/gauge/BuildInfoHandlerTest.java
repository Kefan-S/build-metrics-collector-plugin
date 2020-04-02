package io.jenkins.plugins.collector.actions.gauge;

import hudson.model.Result;
import io.jenkins.plugins.collector.builder.MockBuild;
import io.jenkins.plugins.collector.builder.MockBuildBuilder;
import io.jenkins.plugins.collector.util.CustomizeMetrics;
import io.prometheus.client.Collector.MetricFamilySamples.Sample;
import io.prometheus.client.Gauge;
import java.util.Arrays;
import java.util.Collection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static io.jenkins.plugins.collector.config.Constant.METRICS_LABEL_NAME_ARRAY;
import static io.jenkins.plugins.collector.config.Constant.METRICS_NAMESPACE;
import static io.jenkins.plugins.collector.config.Constant.METRICS_NAME_PREFIX;
import static io.jenkins.plugins.collector.config.Constant.METRICS_SUBSYSTEM;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;

class BuildInfoHandlerTest {

  Gauge durationGauge;
  ;
  Gauge startTimeGauge;

  @BeforeEach
  void setUp() {
    durationGauge = Gauge.build()
        .name(METRICS_NAME_PREFIX + "_last_build_duration_in_milliseconds")
        .subsystem(METRICS_SUBSYSTEM).namespace(METRICS_NAMESPACE)
        .labelNames(METRICS_LABEL_NAME_ARRAY)
        .help("One build duration in milliseconds")
        .create();

    startTimeGauge = Gauge.build()
        .name(METRICS_NAME_PREFIX + "_last_build_start_timestamp")
        .subsystem(METRICS_SUBSYSTEM).namespace(METRICS_NAMESPACE)
        .labelNames(METRICS_LABEL_NAME_ARRAY)
        .help("One build start timestamp")
        .create();
  }

  @Test
  void should_throws_illgegalAragumentException_while_label_count_less_than_required() {
    MockBuild mockBuild = new MockBuildBuilder()
        .startTimeInMillis(100)
        .duration(100)
        .result(Result.SUCCESS)
        .create();

    String[] labels = Arrays.copyOf(METRICS_LABEL_NAME_ARRAY, METRICS_LABEL_NAME_ARRAY.length - 1);
    CustomizeMetrics mockMetrics = Mockito.mock(CustomizeMetrics.class);
    assertThrows(IllegalArgumentException.class, () -> {
      new BuildInfoHandler(mockMetrics, durationGauge, startTimeGauge)
          .accept(labels, mockBuild);
    });
    Mockito.verify(mockMetrics, Mockito.times(0)).addCollector(any(Gauge.class));
  }

  @Test
  void should_throws_illgegalAragumentException_while_label_count_more_than_required() {
    MockBuild mockBuild = new MockBuildBuilder()
        .startTimeInMillis(100)
        .duration(100)
        .result(Result.SUCCESS)
        .create();

    String[] labels = Arrays.copyOf(METRICS_LABEL_NAME_ARRAY, METRICS_LABEL_NAME_ARRAY.length + 1);
    CustomizeMetrics mockMetrics = Mockito.mock(CustomizeMetrics.class);
    assertThrows(IllegalArgumentException.class, () -> {
      new BuildInfoHandler(mockMetrics, durationGauge, startTimeGauge)
          .accept(labels, mockBuild);
    });
    Mockito.verify(mockMetrics, Mockito.times(0)).addCollector(any(Gauge.class));
  }

  @Test
  void should_push_two_samples_to_collection_while_parameter_was_passed_correctly() {
    MockBuild mockBuild = new MockBuildBuilder()
        .startTimeInMillis(100)
        .duration(50)
        .result(Result.SUCCESS)
        .create();
    CustomizeMetrics mockMetrics = Mockito.mock(CustomizeMetrics.class);

    new BuildInfoHandler(mockMetrics, durationGauge, startTimeGauge).accept(METRICS_LABEL_NAME_ARRAY, mockBuild);
    Mockito.verify(mockMetrics, Mockito.times(2)).addCollector(any(Gauge.class));
    Sample[] durationSamples = durationGauge.collect().stream().map(familySamples -> familySamples.samples).flatMap(Collection::stream).toArray(Sample[]::new);
    Sample[] startTimeSamples = startTimeGauge.collect().stream().map(familySamples -> familySamples.samples).flatMap(Collection::stream).toArray(Sample[]::new);
    assertArrayEquals(durationSamples, new Sample[]{
        new Sample(
            "default_jenkins_builds_last_build_duration_in_milliseconds",
            Arrays.asList("jenkins_job", "triggeredBy", "result"),
            Arrays.asList("jenkins_job", "triggeredBy", "result"),
            50L,
            null
        )
    });

    assertArrayEquals(startTimeSamples, new Sample[]{
        new Sample(
            "default_jenkins_builds_last_build_start_timestamp",
            Arrays.asList("jenkins_job", "triggeredBy", "result"),
            Arrays.asList("jenkins_job", "triggeredBy", "result"),
            100L,
            null
        )
    });
  }
}
