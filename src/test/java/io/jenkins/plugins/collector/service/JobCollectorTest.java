package io.jenkins.plugins.collector.service;

import hudson.model.Run;
import io.jenkins.plugins.collector.data.BuildProvider;
import io.jenkins.plugins.collector.data.CustomizeMetrics;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.function.Consumer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
@RunWith(MockitoJUnitRunner.class)
public class JobCollectorTest {

  @Mock
  private CustomizeMetrics customizeMetrics;
  @Mock
  private Consumer<Run> buildHandler;
  @Mock
  private BuildProvider buildProvider;
  @InjectMocks
  private JobCollector jobCollector;

  @Test
  public void should_never_handle_when_collect_given_empty_builds() {

    when(buildProvider.getNeedToHandleBuilds()).thenReturn(Collections.emptyList());

    jobCollector.collect();
    verify(customizeMetrics).initMetrics();
    verify(customizeMetrics).getMetricsList();
    verify(buildHandler, never()).accept(any());
    verify(buildProvider, never()).remove(any());
  }

  @Test
  public void should_handle_correctly_when_collect_given_not_empty_builds() {

    Run buildOne = mock(Run.class);
    Run buildTwo = mock(Run.class);
    when(buildProvider.getNeedToHandleBuilds()).thenReturn(new ArrayList<>(Arrays.asList(buildOne, buildTwo)));

    jobCollector.collect();
    verify(customizeMetrics).initMetrics();
    verify(customizeMetrics).getMetricsList();
    verify(buildHandler, times(2)).accept(any());
    verify(buildHandler).accept(buildOne);
    verify(buildHandler).accept(buildTwo);
    verify(buildProvider, times(2)).remove(any());
    verify(buildProvider).remove(buildOne);
    verify(buildProvider).remove(buildTwo);
  }

}