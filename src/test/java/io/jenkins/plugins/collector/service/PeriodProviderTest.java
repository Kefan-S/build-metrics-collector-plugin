package io.jenkins.plugins.collector.service;

import io.jenkins.plugins.collector.config.PrometheusConfiguration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.junit.Assert.assertEquals;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest(value = {PrometheusConfiguration.class})
public class PeriodProviderTest {

  @Test
  public void should_get_period_correctly_when_get_period_given_current_and_previous_period_equal() {
    PeriodProvider periodProvider = new PeriodProvider(100, 100);

    assertEquals(100, periodProvider.getPeriodInSeconds());
  }

  @Test
  public void should_get_period_correctly_and_update_period_when_get_period_given_current_and_previous_period_not_equal() {
    PeriodProvider periodProvider = new PeriodProvider(100, 1000);

    long result = periodProvider.getPeriodInSeconds();
    assertEquals(1100, result);
    assertEquals(new PeriodProvider(1000, 1000), periodProvider);
  }

  @Test
  public void should_update_period_correctly_when_update_period() {
    PeriodProvider periodProvider = new PeriodProvider(100, 100);
    PowerMockito.mockStatic(PrometheusConfiguration.class);
    PrometheusConfiguration mockPrometheusConfiguration = mock(PrometheusConfiguration.class);
    when(PrometheusConfiguration.get()).thenReturn(mockPrometheusConfiguration);
    when(mockPrometheusConfiguration.getCollectingMetricsPeriodInSeconds()).thenReturn(1000L);

    periodProvider.updatePeriods();

    assertEquals(new PeriodProvider(100, 1000), periodProvider);
  }

}