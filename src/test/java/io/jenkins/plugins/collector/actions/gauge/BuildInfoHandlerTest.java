package io.jenkins.plugins.collector.actions.gauge;

import hudson.model.Result;
import io.jenkins.plugins.collector.builder.MockBuild;
import io.jenkins.plugins.collector.builder.MockBuildBuilder;
import io.jenkins.plugins.collector.util.CustomizeMetrics;
import io.prometheus.client.Gauge;
import io.prometheus.client.Gauge.Child;
import java.util.Arrays;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static io.jenkins.plugins.collector.config.Constant.METRICS_LABEL_NAME_ARRAY;
import static io.jenkins.plugins.collector.config.Constant.METRICS_NAMESPACE;
import static io.jenkins.plugins.collector.config.Constant.METRICS_NAME_PREFIX;
import static io.jenkins.plugins.collector.config.Constant.METRICS_SUBSYSTEM;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;

class BuildInfoHandlerTest {

  Gauge durationGauge;
  Gauge startTimeGauge;
  private Child durationGaugeChild;
  private Child startTimeGaugeChild;
  private MockBuild mockBuild;
  private CustomizeMetrics mockMetrics;

  @BeforeEach
  void setUp() {
    durationGauge = Mockito.spy(Gauge.build()
        .name(METRICS_NAME_PREFIX + "_last_build_duration_in_milliseconds")
        .subsystem(METRICS_SUBSYSTEM).namespace(METRICS_NAMESPACE)
        .labelNames(METRICS_LABEL_NAME_ARRAY)
        .help("One build duration in milliseconds")
        .create());

    startTimeGauge = Mockito.spy(Gauge.build()
        .name(METRICS_NAME_PREFIX + "_last_build_start_timestamp")
        .subsystem(METRICS_SUBSYSTEM).namespace(METRICS_NAMESPACE)
        .labelNames(METRICS_LABEL_NAME_ARRAY)
        .help("One build start timestamp")
        .create());

    mockBuild = new MockBuildBuilder()
        .startTimeInMillis(100)
        .duration(50)
        .result(Result.SUCCESS)
        .create();

    durationGaugeChild = Mockito.mock(Child.class);
    startTimeGaugeChild = Mockito.mock(Child.class);
    mockMetrics = Mockito.mock(CustomizeMetrics.class);

    Mockito.doAnswer(invocationOnMock -> {
      invocationOnMock.callRealMethod();
      return durationGaugeChild;
    }).when(durationGauge).labels(any());

    Mockito.doAnswer(invocationOnMock -> {
      invocationOnMock.callRealMethod();
      return startTimeGaugeChild;
    }).when(startTimeGauge).labels(any());
  }

  @Test
  void should_throws_illgegalAragumentException_while_label_count_less_than_required() {
    String[] labels = Arrays.copyOf(METRICS_LABEL_NAME_ARRAY, METRICS_LABEL_NAME_ARRAY.length - 1);
    assertThrows(IllegalArgumentException.class, () -> {
      new BuildInfoHandler(mockMetrics, durationGauge, startTimeGauge)
          .accept(labels, mockBuild);
    });
    Mockito.verify(mockMetrics, Mockito.times(0)).addCollector(any(Gauge.class));
    Mockito.verify(durationGauge, Mockito.times(1)).labels(labels);
    Mockito.verify(startTimeGauge, Mockito.times(0)).labels(labels);
    Mockito.verify(durationGaugeChild, Mockito.times(0)).set(50L);
    Mockito.verify(startTimeGaugeChild, Mockito.times(0)).set(100L);
  }

  @Test
  void should_throws_illgegalAragumentException_while_label_count_more_than_required() {
    String[] labels = Arrays.copyOf(METRICS_LABEL_NAME_ARRAY, METRICS_LABEL_NAME_ARRAY.length + 1);
    assertThrows(IllegalArgumentException.class, () -> {
      new BuildInfoHandler(mockMetrics, durationGauge, startTimeGauge).accept(labels, mockBuild);
    });
    Mockito.verify(mockMetrics, Mockito.times(0)).addCollector(any(Gauge.class));
    Mockito.verify(durationGauge, Mockito.times(1)).labels(labels);
    Mockito.verify(startTimeGauge, Mockito.times(0)).labels(labels);
    Mockito.verify(durationGaugeChild, Mockito.times(0)).set(50L);
    Mockito.verify(startTimeGaugeChild, Mockito.times(0)).set(100L);
  }

  @Test
  void should_push_two_samples_to_collection_while_parameter_was_passed_correctly() {
    new BuildInfoHandler(mockMetrics, durationGauge, startTimeGauge).accept(METRICS_LABEL_NAME_ARRAY, mockBuild);
    Mockito.verify(mockMetrics, Mockito.times(2)).addCollector(any(Gauge.class));
    Mockito.verify(durationGauge, Mockito.times(1)).labels(METRICS_LABEL_NAME_ARRAY);
    Mockito.verify(startTimeGauge, Mockito.times(1)).labels(METRICS_LABEL_NAME_ARRAY);
    Mockito.verify(durationGaugeChild, Mockito.times(1)).set(50L);
    Mockito.verify(startTimeGaugeChild, Mockito.times(1)).set(100L);
  }
}
