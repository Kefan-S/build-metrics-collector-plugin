package io.jenkins.plugins.collector.actions.gauge;

import hudson.model.Result;
import io.jenkins.plugins.collector.builder.MockBuild;
import io.jenkins.plugins.collector.builder.MockBuildBuilder;
import io.jenkins.plugins.collector.util.CustomizeMetrics;
import io.prometheus.client.Gauge;
import io.prometheus.client.Gauge.Child;
import java.util.Arrays;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import static io.jenkins.plugins.collector.config.Constant.METRICS_LABEL_NAME_ARRAY;
import static io.jenkins.plugins.collector.config.Constant.METRICS_NAMESPACE;
import static io.jenkins.plugins.collector.config.Constant.METRICS_NAME_PREFIX;
import static io.jenkins.plugins.collector.config.Constant.METRICS_SUBSYSTEM;
import static org.mockito.ArgumentMatchers.any;

public class BuildInfoHandlerTest {

  Gauge durationGauge;
  Gauge startTimeGauge;
  private Child durationGaugeChild;
  private Child startTimeGaugeChild;
  private MockBuild mockBuild;
  private CustomizeMetrics mockMetrics;

  @Before
  public void setUp() {
    durationGauge = Mockito.spy(Gauge.build()
        .name(METRICS_NAME_PREFIX + "_last_build_duration_in_milliseconds")
        .subsystem(METRICS_SUBSYSTEM).namespace(METRICS_NAMESPACE)
        .labelNames((String[]) METRICS_LABEL_NAME_ARRAY.toArray())
        .help("One build duration in milliseconds")
        .create());

    startTimeGauge = Mockito.spy(Gauge.build()
        .name(METRICS_NAME_PREFIX + "_last_build_start_timestamp")
        .subsystem(METRICS_SUBSYSTEM).namespace(METRICS_NAMESPACE)
        .labelNames((String[]) METRICS_LABEL_NAME_ARRAY.toArray())
        .help("One build start timestamp")
        .create());

    mockBuild = new MockBuildBuilder()
        .startTimeInMillis(100)
        .duration(50)
        .result(Result.SUCCESS)
        .create();

    durationGaugeChild = Mockito.mock(Child.class);
    startTimeGaugeChild = Mockito.mock(Child.class);

    Mockito.doAnswer(invocationOnMock -> {
      invocationOnMock.callRealMethod();
      return durationGaugeChild;
    }).when(durationGauge).labels(any());

    Mockito.doAnswer(invocationOnMock -> {
      invocationOnMock.callRealMethod();
      return startTimeGaugeChild;
    }).when(startTimeGauge).labels(any());
  }

  @Test(expected = IllegalArgumentException.class)
  public void should_throws_illegalArgumentException_while_label_count_less_than_required() {
    String[] labels = Arrays.copyOf((String[]) METRICS_LABEL_NAME_ARRAY.toArray(), METRICS_LABEL_NAME_ARRAY.size() - 1);

    new BuildInfoHandler(durationGauge, startTimeGauge).accept(labels, mockBuild);

    Mockito.verify(durationGauge, Mockito.times(1)).labels(labels);
    Mockito.verify(startTimeGauge, Mockito.times(0)).labels(labels);
    Mockito.verify(durationGaugeChild, Mockito.times(0)).set(50L);
    Mockito.verify(startTimeGaugeChild, Mockito.times(0)).set(100L);
  }

  @Test(expected = IllegalArgumentException.class)
  public void should_throws_illegalArgumentException_while_label_count_more_than_required() {
    String[] labels = Arrays.copyOf((String[]) METRICS_LABEL_NAME_ARRAY.toArray(), METRICS_LABEL_NAME_ARRAY.size() + 1);

    new BuildInfoHandler(durationGauge, startTimeGauge).accept(labels, mockBuild);

    Mockito.verify(durationGauge, Mockito.times(1)).labels(labels);
    Mockito.verify(startTimeGauge, Mockito.times(0)).labels(labels);
    Mockito.verify(durationGaugeChild, Mockito.times(0)).set(50L);
    Mockito.verify(startTimeGaugeChild, Mockito.times(0)).set(100L);
  }

  @Test
  public void should_push_two_samples_to_collection_while_parameter_was_passed_correctly() {
    new BuildInfoHandler(durationGauge, startTimeGauge).accept((String[]) METRICS_LABEL_NAME_ARRAY.toArray(), mockBuild);
    Mockito.verify(durationGauge, Mockito.times(1)).labels((String[]) METRICS_LABEL_NAME_ARRAY.toArray());
    Mockito.verify(startTimeGauge, Mockito.times(1)).labels((String[]) METRICS_LABEL_NAME_ARRAY.toArray());
    Mockito.verify(durationGaugeChild, Mockito.times(1)).set(50L);
    Mockito.verify(startTimeGaugeChild, Mockito.times(1)).set(100L);
  }
}
