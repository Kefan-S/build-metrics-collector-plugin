package io.jenkins.plugins.collector.data;

import hudson.model.Item;
import hudson.model.Job;
import hudson.model.Run;
import hudson.util.RunList;
import io.jenkins.plugins.collector.config.PrometheusConfiguration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import jenkins.model.Jenkins;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Jenkins.class, Instant.class, PrometheusConfiguration.class})
public class JobCollectorTest {

  @Mock
  private CustomizeMetrics customizeMetrics;

  @Mock
  private Consumer<Run> buildHandler;

  @InjectMocks
  private JobCollector jobCollector;

  @Mock
  private Item item;

  @Before
  public void setUp() {

    mockStatic(PrometheusConfiguration.class);
    PrometheusConfiguration prometheusConfiguration = mock(PrometheusConfiguration.class);
    when(PrometheusConfiguration.get()).thenReturn(prometheusConfiguration);
    when(prometheusConfiguration.getCollectingMetricsPeriodInSeconds()).thenReturn(15L);

    mockStatic(Jenkins.class);
    Jenkins jenkins = mock(Jenkins.class);
    when(Jenkins.getInstanceOrNull()).thenReturn(jenkins);
    List<Item> items = Arrays.asList(item);
    when(jenkins.getAllItems()).thenReturn(items);
  }

  @Test
  public void should_never_call_handleBuild_when_collect_given_there_is_no_job() {

    when(item.getAllJobs()).thenReturn(Collections.EMPTY_LIST);

    jobCollector.collect();

    verify(buildHandler, never()).accept(any());
    verify(customizeMetrics, times(1)).getMetricsList();
  }

  @Test
  public void should_never_call_handleBuild_when_collect_given_there_is_one_unbuildable_job() {

    Job job = mock(Job.class);
    Collection jobs = new ArrayList();
    jobs.add(job);
    when(item.getAllJobs()).thenReturn(jobs);
    when(job.isBuildable()).thenReturn(false);

    jobCollector.collect();

    verify(buildHandler, never()).accept(any());
    verify(customizeMetrics, times(1)).getMetricsList();
  }

  @Test
  public void should_never_call_handleBuild_when_collect_given_uncompletedBuildsMap_is_empty_and_there_is_one_job_and_no_build() {

    Job job = mock(Job.class, Answers.RETURNS_DEEP_STUBS);
    Collection jobs = new ArrayList();
    jobs.add(job);
    when(item.getAllJobs()).thenReturn(jobs);
    when(job.isBuildable()).thenReturn(true);
    when(job.getFullName()).thenReturn("job full name");

    when(job.getBuilds().byTimestamp(anyLong(), anyLong())).thenReturn(RunList.fromRuns(Collections.EMPTY_LIST));

    jobCollector.collect();

    verify(buildHandler, never()).accept(any());
    verify(customizeMetrics, times(1)).getMetricsList();
  }

  @Test
  public void should_call_handleBuild_one_time_when_collect_given_uncompletedBuildsMap_is_empty_and_there_is_one_job_and_one_build() {

    Job job = mock(Job.class, Answers.RETURNS_DEEP_STUBS);
    Collection jobs = new ArrayList();
    jobs.add(job);
    when(item.getAllJobs()).thenReturn(jobs);
    when(job.isBuildable()).thenReturn(true);
    when(job.getFullName()).thenReturn("job full name");

    List<Run> runs = new ArrayList<>();
    Run run = mock(Run.class);
    runs.add(run);
    RunList runList = RunList.fromRuns(runs);
    when(job.getBuilds().byTimestamp(anyLong(), anyLong())).thenReturn(runList);

    jobCollector.collect();

    verify(buildHandler, times(1)).accept(run);
    verify(customizeMetrics, times(1)).getMetricsList();
  }

  @Test
  public void should_call_handleBuild_one_time_by_uncompeted_build_when_collect_given_uncompletedBuildsMap_has_one_build_and_there_is_one_job_and_one_build() {

    Job job = mock(Job.class, Answers.RETURNS_DEEP_STUBS);
    Collection jobs = new ArrayList();
    jobs.add(job);
    when(item.getAllJobs()).thenReturn(jobs);
    when(job.isBuildable()).thenReturn(true);
    when(job.getFullName()).thenReturn("job full name");

    List<Run> uncompletedRuns = new ArrayList<>();
    Run uncompletedRun = mock(Run.class);
    uncompletedRuns.add(uncompletedRun);
    Map<String, List<Run>> uncompletedBuildsMap = new HashMap<>();
    uncompletedBuildsMap.put("job full name", uncompletedRuns);
    Whitebox.setInternalState(jobCollector, "uncompletedBuildsMap", uncompletedBuildsMap);

    List<Run> runs = new ArrayList<>();
    Run run = mock(Run.class);
    runs.add(run);
    RunList runList = RunList.fromRuns(runs);
    when(job.getBuilds().byTimestamp(anyLong(), anyLong())).thenReturn(runList);

    jobCollector.collect();

    verify(buildHandler, times(1)).accept(uncompletedRun);
    verify(customizeMetrics, times(1)).getMetricsList();
  }

  @Test
  public void should_call_handleBuild_one_time_by_first_build_when_collect_given_uncompletedBuildsMap_is_empty_and_there_is_one_job_and_two_builds() {

    Job job = mock(Job.class, Answers.RETURNS_DEEP_STUBS);
    Collection jobs = new ArrayList();
    jobs.add(job);
    when(item.getAllJobs()).thenReturn(jobs);
    when(job.isBuildable()).thenReturn(true);
    when(job.getFullName()).thenReturn("job full name");

    List<Run> runs = new ArrayList<>();
    Run firstRun = mock(Run.class);
    Run secondRun = mock(Run.class);
    runs.add(firstRun);
    runs.add(secondRun);
    RunList runList = RunList.fromRuns(runs);
    when(job.getBuilds().byTimestamp(anyLong(), anyLong())).thenReturn(runList);

    jobCollector.collect();

    verify(buildHandler, times(1)).accept(firstRun);
    verify(customizeMetrics, times(1)).getMetricsList();
  }

}
