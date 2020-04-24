package io.jenkins.plugins.collector.service;

import io.jenkins.plugins.collector.config.PrometheusConfiguration;
import java.util.Timer;
import java.util.TimerTask;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest(value = {PrometheusConfiguration.class})
public class AsyncWorkerManagerTest {

  @Mock
  private PrometheusMetrics prometheusMetrics;

  @InjectMocks
  private AsyncWorkerManager asyncWorkerManager;

  @Before
  public void setup() {
    PowerMockito.mockStatic(PrometheusConfiguration.class);
    PrometheusConfiguration mockPrometheusConfiguration = mock(PrometheusConfiguration.class);
    when(PrometheusConfiguration.get()).thenReturn(mockPrometheusConfiguration);
    when(mockPrometheusConfiguration.getCollectingMetricsPeriodInSeconds()).thenReturn(20L);
  }

  @Test
  public void should_cancel_old_timer_task_and_schedule_new_task_when_update_async_worker() {
    Timer mockTimer = mock(Timer.class);
    Whitebox.setInternalState(asyncWorkerManager, "timer", mockTimer);
    AsyncWork mockAsyncWork = mock(AsyncWork.class);
    Whitebox.setInternalState(asyncWorkerManager, "timerTask", mockAsyncWork);

    asyncWorkerManager.updateAsyncWorker();
    verify(mockAsyncWork).cancel();
    verify(mockTimer).schedule(any(TimerTask.class), anyLong(), anyLong());
    assertNotEquals(mockAsyncWork, asyncWorkerManager.timerTask);
  }

  @Test
  public void should_set_up_timer_and_timer_task_when_init() {
    asyncWorkerManager.init();

    assertNotNull(asyncWorkerManager.timerTask);
    assertNotNull(asyncWorkerManager.timer);
  }

}