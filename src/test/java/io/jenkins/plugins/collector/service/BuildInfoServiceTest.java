package io.jenkins.plugins.collector.service;

import hudson.model.Run;
import io.jenkins.plugins.collector.data.BuildProvider;
import io.jenkins.plugins.collector.model.BuildInfo;
import io.jenkins.plugins.collector.util.BuildUtil;
import java.util.LinkedList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ BuildUtil.class, LeadTimeCalculate.class, RecoverTimeCalculate.class})
public class BuildInfoServiceTest {
  @Mock
  LeadTimeCalculate leadTimeCalculate;
  @Mock
  RecoverTimeCalculate recoverTimeCalculate;
  @Mock
  BuildProvider buildProvider;
  @InjectMocks
  private BuildInfoService buildInfoService;

  Long duration = 1L;
  Long leadTime = 2L;
  Long recoverTime = 3L;
  Long startTime = 4L;
  Run fakeRun;

  @Before
  public void setup() {
    fakeRun = Mockito.mock(Run.class, Answers.RETURNS_DEEP_STUBS);
    when(fakeRun.getDuration()).thenReturn(duration);
    when(fakeRun.getStartTimeInMillis()).thenReturn(startTime);
    when(leadTimeCalculate.apply(fakeRun)).thenReturn(leadTime);
    when(recoverTimeCalculate.apply(fakeRun)).thenReturn(recoverTime);
    mockStatic(BuildUtil.class);
    when(BuildUtil.getJobName(fakeRun)).thenReturn("buildName");
    when(BuildUtil.getResultValue(fakeRun)).thenReturn("0");
    when(BuildUtil.getTrigger(fakeRun)).thenReturn("user");
  }

  @Test
  public void should_get_buildInfo_when_getBuildInfo_given_a_success_run() {

    BuildInfo buildInfo = buildInfoService.getBuildInfo(fakeRun);

    assertEquals(duration, buildInfo.getDuration());
    assertEquals(recoverTime, buildInfo.getRecoverTime());
    assertEquals(startTime, buildInfo.getStartTime());
    assertEquals(leadTime, buildInfo.getLeadTime());
    assertEquals("buildName", buildInfo.getJenkinsJob());
    assertEquals("0", buildInfo.getResult());
    assertEquals("user", buildInfo.getTriggeredBy());

  }

  @Test
  public void should_return_all_buildInfo_when_get_all_buildInfo_given_builds_need_to_handle() {
    List<Run> buildsNeedToHandle = new LinkedList<Run>();
    buildsNeedToHandle.add(fakeRun);
    when(buildProvider.getNeedToHandleBuilds()).thenReturn(buildsNeedToHandle);

    List<BuildInfo> allBuildInfo = buildInfoService.getAllBuildInfo();

    assertEquals(duration, allBuildInfo.get(0).getDuration());
    assertEquals(recoverTime, allBuildInfo.get(0).getRecoverTime());
    assertEquals(startTime, allBuildInfo.get(0).getStartTime());
    assertEquals(leadTime, allBuildInfo.get(0).getLeadTime());
    assertEquals("buildName", allBuildInfo.get(0).getJenkinsJob());
    assertEquals("0", allBuildInfo.get(0).getResult());
    assertEquals("user", allBuildInfo.get(0).getTriggeredBy());
    Mockito.verify(buildProvider, times(1)).remove(fakeRun);
  }
}
