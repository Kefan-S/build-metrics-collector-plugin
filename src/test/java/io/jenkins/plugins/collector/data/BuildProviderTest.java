package io.jenkins.plugins.collector.data;

import hudson.model.Job;
import hudson.model.Run;
import hudson.util.RunList;
import io.jenkins.plugins.collector.exception.NoSuchBuildException;
import io.jenkins.plugins.collector.service.PeriodProvider;
import java.util.ArrayList;
import java.util.Arrays;
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
  @InjectMocks
  private BuildProvider buildProvider;

  @Test
  public void should_return_empty_builds_when_get_all_need_to_handle_builds_given_empty_jobs_and_empty_unHandle_map() {

    when(jobProvider.getAllJobs()).thenReturn(Collections.emptyList());
    List<Run> result = buildProvider.getNeedToHandleBuilds();
    assertEquals(0, result.size());
  }

  @Test
  public void should_return_empty_builds_when_get_all_need_to_handle_builds_given_jobs_have_no_builds_and_empty_unHandle_map() {

    Job mockJob = mock(Job.class, RETURNS_DEEP_STUBS);
    when(jobProvider.getAllJobs()).thenReturn(new ArrayList<>(Arrays.asList(mockJob)));
    when(mockJob.getBuilds().byTimestamp(anyLong(), anyLong())).thenReturn(RunList.fromRuns(Collections.emptyList()));

    List<Run> result = buildProvider.getNeedToHandleBuilds();
    assertEquals(0, result.size());
  }

  @Test
  public void should_return_correct_builds_when_get_all_need_to_handle_builds_given_jobs_have_builds_and_empty_unHandle_map() {

    Job mockJob = mock(Job.class, RETURNS_DEEP_STUBS);
    when(jobProvider.getAllJobs()).thenReturn(new ArrayList<>(Arrays.asList(mockJob)));
    Run mockRun = mock(Run.class);
    when(mockJob.getBuilds().byTimestamp(anyLong(), anyLong())).thenReturn(RunList.fromRuns(new ArrayList<>(Arrays.asList(mockRun))));

    List<Run> result = buildProvider.getNeedToHandleBuilds();
    assertEquals(new ArrayList(Arrays.asList(mockRun)), result);
  }

  @Test
  public void should_return_correct_builds_when_get_all_need_to_handle_builds_given_jobs_have_builds_and_not_empty_unHandle_map() {
    Map<String, Set<Run>> fakeBuildsMap = new HashMap();
    Run mockExistingBuild = mock(Run.class);
    fakeBuildsMap.put("full-name", new HashSet(Arrays.asList(mockExistingBuild)) {
    });
    Whitebox.setInternalState(buildProvider, "jobFullNameToUnhandledBuildsMap", fakeBuildsMap);
    Job mockJob = mock(Job.class, RETURNS_DEEP_STUBS);
    when(jobProvider.getAllJobs()).thenReturn(new ArrayList<>(Arrays.asList(mockJob)));
    when(mockJob.getFullName()).thenReturn("full-name");
    when(mockJob.getBuilds().byTimestamp(anyLong(), anyLong())).thenReturn(RunList.fromRuns(new ArrayList<>(Arrays.asList(mock(Run.class)))));

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
    fakeBuildsMap.put("full-name", new HashSet(Arrays.asList(existRun)));
    Whitebox.setInternalState(buildProvider, "jobFullNameToUnhandledBuildsMap", fakeBuildsMap);

    when(existRun.getParent().getFullName()).thenReturn("full-name");

    buildProvider.remove(existRun);

    assertEquals(0, fakeBuildsMap.get("full-name").size());
  }
}
