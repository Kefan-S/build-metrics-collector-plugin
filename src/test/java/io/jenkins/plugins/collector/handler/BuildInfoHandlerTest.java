package io.jenkins.plugins.collector.handler;

import hudson.model.Result;
import io.jenkins.plugins.collector.builder.MockBuild;
import io.jenkins.plugins.collector.builder.MockBuildBuilder;
import io.jenkins.plugins.collector.util.BuildUtil;
import io.prometheus.client.Gauge;
import io.prometheus.client.Gauge.Child;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static io.jenkins.plugins.collector.config.Constant.METRICS_LABEL_NAME_ARRAY;
import static io.jenkins.plugins.collector.config.Constant.METRICS_NAMESPACE;
import static io.jenkins.plugins.collector.config.Constant.METRICS_NAME_PREFIX;
import static io.jenkins.plugins.collector.config.Constant.METRICS_SUBSYSTEM;
import static io.jenkins.plugins.collector.handler.LeadTimeHandlerTest.LEADTIME_HANDLER_LABELS;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({BuildUtil.class})
public class BuildInfoHandlerTest {

  Gauge durationGauge;
  Gauge startTimeGauge;
  private Child durationGaugeChild;
  private Child startTimeGaugeChild;
  private MockBuild mockBuild;

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

    PowerMockito.mockStatic(BuildUtil.class);
    when(BuildUtil.getLabels(any())).thenReturn(LEADTIME_HANDLER_LABELS);
  }

  @Test
  public void should_push_two_samples_to_collection_while_parameter_was_passed_correctly() {
    new BuildInfoHandler(durationGauge, startTimeGauge).accept(mockBuild);
    Mockito.verify(durationGauge, Mockito.times(1)).labels((String[]) METRICS_LABEL_NAME_ARRAY.toArray());
    Mockito.verify(startTimeGauge, Mockito.times(1)).labels((String[]) METRICS_LABEL_NAME_ARRAY.toArray());
    Mockito.verify(durationGaugeChild, Mockito.times(1)).set(50L);
    Mockito.verify(startTimeGaugeChild, Mockito.times(1)).set(100L);
  }
}
