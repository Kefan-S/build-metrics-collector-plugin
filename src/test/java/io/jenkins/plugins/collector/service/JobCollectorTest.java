package io.jenkins.plugins.collector.service;

import hudson.model.Run;
import io.jenkins.plugins.collector.data.BuildProvider;
import io.prometheus.client.Collector.MetricFamilySamples;
import io.prometheus.client.Gauge;
import io.prometheus.client.SimpleCollector;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import static com.google.common.collect.Lists.newArrayList;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class JobCollectorTest {

  @Mock
  private List<Function<Run, List<SimpleCollector>>> buildHandler;

  @Mock
  private BuildProvider buildProvider;

  @InjectMocks
  private JobCollector jobCollector;

  @Test
  public void should_never_handle_when_collect_given_empty_builds() {
    when(buildProvider.getNeedToHandleBuilds()).thenReturn(Collections.emptyList());

    final List<MetricFamilySamples> actual = jobCollector.collect();

    assertEquals(0, actual.size());
    verify(buildProvider, never()).remove(any());
  }

  @Test
  public void should_handle_correctly_when_collect_given_not_empty_builds() {

    Run buildOne = mock(Run.class);
    Run buildTwo = mock(Run.class);
    when(buildProvider.getNeedToHandleBuilds()).thenReturn(new ArrayList<>(Arrays.asList(buildOne, buildTwo)));

    final Function leadTimeHandler = mock(Function.class);
    Gauge gaugeMetric1 = Gauge.build().name("name1").labelNames("labelName1").help("help1").create();
    when(leadTimeHandler.apply(any())).thenReturn(newArrayList(gaugeMetric1));

    Gauge gaugeMetric2 = Gauge.build().name("name2").labelNames("labelName2").help("help2").create();
    final Function recoverTimeHandler = mock(Function.class);
    when(recoverTimeHandler.apply(any())).thenReturn(newArrayList(gaugeMetric2));

    Gauge gaugeMetric3 = Gauge.build().name("name3").labelNames("labelName3").help("help3").create();
    Gauge gaugeMetric4 = Gauge.build().name("name4").labelNames("labelName4").help("help4").create();
    final Function buildInfoHandler = mock(Function.class);
    when(buildInfoHandler.apply(any())).thenReturn(newArrayList(gaugeMetric3, gaugeMetric4));

    Answer<Stream> answer = invocation -> Stream.of(leadTimeHandler, buildInfoHandler, recoverTimeHandler);
    when(buildHandler.stream()).thenAnswer(answer);

    final List<MetricFamilySamples> actual = jobCollector.collect();

    assertEquals(8, actual.size());
    verify(buildProvider, times(2)).remove(any());
    verify(buildProvider).remove(buildOne);
    verify(buildProvider).remove(buildTwo);
  }
}