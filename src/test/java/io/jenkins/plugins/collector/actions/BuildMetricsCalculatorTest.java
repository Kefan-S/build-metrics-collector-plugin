package io.jenkins.plugins.collector.actions;

import hudson.model.Run;
import io.jenkins.plugins.collector.util.BuildUtil;
import java.util.function.BiConsumer;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static io.jenkins.plugins.collector.config.Constant.METRICS_LABEL_NAME_ARRAY;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;


@RunWith(PowerMockRunner.class)
@PrepareForTest({BuildUtil.class})
public class BuildMetricsCalculatorTest {

  @Mock
  private BiConsumer<String[], Run> successBuildHandler;

  @Mock
  private BiConsumer<String[], Run> buildInfoHandler;

  private BuildMetricsCalculator buildMetricsCalculator;

  @Before
  public void setUp() {
    buildMetricsCalculator = new BuildMetricsCalculator(buildInfoHandler, successBuildHandler);
  }

  @Test
  public void should_not_invoke_build_handler_when_handle_build_given_build_is_null() {
    buildMetricsCalculator.handleBuild(null);

    verify(successBuildHandler, never()).accept(any(), any());
    verify(buildInfoHandler, never()).accept(any(), any());
  }

  @Test
  public void should_invoke_success_build_handler_when_handle_build_given_build_is_successful() {
    PowerMockito.mockStatic(BuildUtil.class);
    PowerMockito.when(BuildUtil.getLabels(any())).thenReturn((String[]) METRICS_LABEL_NAME_ARRAY.toArray());
    PowerMockito.when(BuildUtil.isSuccessfulBuild(any())).thenReturn(true);

    buildMetricsCalculator.handleBuild(mock(Run.class));

    verify(successBuildHandler, times(1)).accept(any(), any());
    verify(buildInfoHandler, times(1)).accept(any(), any());
  }

  @Test
  public void should_not_invoke_success_build_handler_when_handle_build_given_build_is_failed() {
    PowerMockito.mockStatic(BuildUtil.class);
    PowerMockito.when(BuildUtil.getLabels(any())).thenReturn((String[]) METRICS_LABEL_NAME_ARRAY.toArray());
    PowerMockito.when(BuildUtil.isSuccessfulBuild(any())).thenReturn(false);

    buildMetricsCalculator.handleBuild(mock(Run.class));

    verify(successBuildHandler, never()).accept(any(), any());
    verify(buildInfoHandler, times(1)).accept(any(), any());
  }
}