package io.jenkins.plugins.collector.data;

import hudson.model.Job;
import hudson.model.Run;
import hudson.util.RunList;
import io.jenkins.plugins.collector.config.PrometheusConfiguration;
import io.jenkins.plugins.collector.exception.NoSuchBuildException;
import io.jenkins.plugins.collector.service.PeriodProvider;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.powermock.reflect.Whitebox;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class BuildProviderTest {

  @Mock
  private JobProvider jobProvider;
  @Mock
  private PeriodProvider periodProvider;
  @Mock
  private PrometheusConfiguration prometheusConfiguration;
  @InjectMocks
  private BuildProvider buildProvider;

  @Test
  public void should_return_empty_builds_when_get_all_need_to_handle_builds_given_empty_jobs_and_empty_unHandle_map() {
    when(jobProvider.getAllJobs()).thenReturn(emptyList());
    when(prometheusConfiguration.getJobName()).thenReturn("jobName");

    List<Run> result = buildProvider.getNeedToHandleBuilds();

    assertEquals(0, result.size());
  }

  @Test
  public void should_return_empty_builds_when_get_all_need_to_handle_builds_given_jobs_have_no_builds_and_empty_unHandle_map() {
    Job mockJob = mock(Job.class, RETURNS_DEEP_STUBS);
    when(mockJob.getFullName()).thenReturn("jobName");
    when(jobProvider.getAllJobs()).thenReturn(new ArrayList<>(asList(mockJob)));
    when(mockJob.getBuilds().byTimestamp(anyLong(), anyLong())).thenReturn(RunList.fromRuns(emptyList()));
    when(prometheusConfiguration.getJobName()).thenReturn("jobName");

    List<Run> result = buildProvider.getNeedToHandleBuilds();

    assertEquals(0, result.size());
  }

  @Test
  public void should_return_correct_builds_when_get_all_need_to_handle_builds_given_jobs_have_builds_and_empty_unHandle_map() {
    Job mockJob = mock(Job.class, RETURNS_DEEP_STUBS);
    when(mockJob.getFullName()).thenReturn("jobName");
    when(jobProvider.getAllJobs()).thenReturn(new ArrayList<>(asList(mockJob)));
    Run mockRun = mock(Run.class);
    when(mockJob.getBuilds().byTimestamp(anyLong(), anyLong())).thenReturn(RunList.fromRuns(new ArrayList<>(asList(mockRun))));
    when(prometheusConfiguration.getJobName()).thenReturn("jobName");

    List<Run> result = buildProvider.getNeedToHandleBuilds();

    assertEquals(new ArrayList(asList(mockRun)), result);
  }

  @Test
  public void should_return_same_builds_when_get_all_need_to_handle_builds_given_the_full_name_of_jobs_are_all_same_to_job_names_from_prometheus_configuration() {
    Job mockJob1 = mock(Job.class, RETURNS_DEEP_STUBS);
    when(mockJob1.getFullName()).thenReturn("jobName1");
    Job mockJob2 = mock(Job.class, RETURNS_DEEP_STUBS);
    when(mockJob2.getFullName()).thenReturn("jobName2");
    when(jobProvider.getAllJobs()).thenReturn(newArrayList(mockJob1, mockJob2));
    Run mockRun1 = mock(Run.class);
    Run mockRun2 = mock(Run.class);
    when(mockJob1.getBuilds().byTimestamp(anyLong(), anyLong())).thenReturn(RunList.fromRuns(new ArrayList<>(asList(mockRun1))));
    when(mockJob2.getBuilds().byTimestamp(anyLong(), anyLong())).thenReturn(RunList.fromRuns(new ArrayList<>(asList(mockRun2))));
    when(prometheusConfiguration.getJobName()).thenReturn("jobName1:jobName2");

    List<Run> result = buildProvider.getNeedToHandleBuilds();

    assertEquals(new ArrayList(asList(mockRun1, mockRun2)), result);
  }

  @Test
  public void should_return_same_builds_when_get_all_need_to_handle_builds_given_the_full_name_of_jobs_with_different_case_from_prometheus_configuration() {
    Job mockJob1 = mock(Job.class, RETURNS_DEEP_STUBS);
    when(mockJob1.getFullName()).thenReturn("jObName1");
    Job mockJob2 = mock(Job.class, RETURNS_DEEP_STUBS);
    when(mockJob2.getFullName()).thenReturn("JobName2");
    when(jobProvider.getAllJobs()).thenReturn(newArrayList(mockJob1, mockJob2));
    Run mockRun1 = mock(Run.class);
    Run mockRun2 = mock(Run.class);
    when(mockJob1.getBuilds().byTimestamp(anyLong(), anyLong())).thenReturn(RunList.fromRuns(new ArrayList<>(asList(mockRun1))));
    when(mockJob2.getBuilds().byTimestamp(anyLong(), anyLong())).thenReturn(RunList.fromRuns(new ArrayList<>(asList(mockRun2))));
    when(prometheusConfiguration.getJobName()).thenReturn("jobNaMe1:jobNAme2");

    List<Run> result = buildProvider.getNeedToHandleBuilds();

    Collections.reverse(result);
    assertEquals(new ArrayList(asList(mockRun1, mockRun2)), result);
  }

  @Test
  public void should_filter_builds_when_get_all_need_to_handle_builds_given_the_full_name_of_jobs_are_not_same_to_job_names_from_prometheus_configuration() {
    Job mockJob1 = mock(Job.class, RETURNS_DEEP_STUBS);
    when(mockJob1.getFullName()).thenReturn("jobName1");
    Job mockJob2 = mock(Job.class, RETURNS_DEEP_STUBS);
    when(mockJob2.getFullName()).thenReturn("jobName2");
    when(jobProvider.getAllJobs()).thenReturn(newArrayList(mockJob1, mockJob2));
    Run mockRun1 = mock(Run.class);
    Run mockRun2 = mock(Run.class);
    when(mockJob1.getBuilds().byTimestamp(anyLong(), anyLong())).thenReturn(RunList.fromRuns(new ArrayList<>(asList(mockRun1))));
    when(mockJob2.getBuilds().byTimestamp(anyLong(), anyLong())).thenReturn(RunList.fromRuns(new ArrayList<>(asList(mockRun2))));
    when(prometheusConfiguration.getJobName()).thenReturn("jobName1");

    List<Run> result = buildProvider.getNeedToHandleBuilds();

    assertEquals(new ArrayList(asList(mockRun1)), result);
  }

  @Test
  public void should_return_correct_builds_when_get_all_need_to_handle_builds_given_jobs_have_builds_and_not_empty_unHandle_map() {
    Map<String, Set<Run>> fakeBuildsMap = new HashMap();
    Run mockExistingBuild = mock(Run.class);
    fakeBuildsMap.put("full-name", new HashSet(asList(mockExistingBuild)));
    Whitebox.setInternalState(buildProvider, "jobFullNameToUnhandledBuildsMap", fakeBuildsMap);
    Job mockJob = mock(Job.class, RETURNS_DEEP_STUBS);
    when(mockJob.getFullName()).thenReturn("jobName");
    when(jobProvider.getAllJobs()).thenReturn(new ArrayList<>(asList(mockJob)));
    when(mockJob.getFullName()).thenReturn("full-name");
    when(mockJob.getBuilds().byTimestamp(anyLong(), anyLong())).thenReturn(RunList.fromRuns(new ArrayList<>(asList(mock(Run.class)))));
    when(prometheusConfiguration.getJobName()).thenReturn("jobName");

    List<Run> result = buildProvider.getNeedToHandleBuilds();
    assertEquals(1, result.size());
  }

  @Test
  public void should_throw_exception_with_message_when_remove_given_build_absent() {
    Run run = mock(Run.class, Answers.RETURNS_DEEP_STUBS);

    when(run.getParent().getFullName()).thenReturn("name");
    when(run.getFullDisplayName()).thenReturn("name#1");

    NoSuchBuildException assertThrow = assertThrows(NoSuchBuildException.class, () -> buildProvider.remove(run));

    assertEquals("No Such Build: name#1", assertThrow.getMessage());
  }

  @Test
  public void should_remove_build_when_remove_given_build_exist() {
    Run existRun = mock(Run.class, Answers.RETURNS_DEEP_STUBS);
    Map<String, Set<Run>> fakeBuildsMap = new HashMap();
    fakeBuildsMap.put("full-name", new HashSet(asList(existRun)));
    Whitebox.setInternalState(buildProvider, "jobFullNameToUnhandledBuildsMap", fakeBuildsMap);

    when(existRun.getParent().getFullName()).thenReturn("full-name");

    buildProvider.remove(existRun);

    assertEquals(0, fakeBuildsMap.get("full-name").size());
  }
}
