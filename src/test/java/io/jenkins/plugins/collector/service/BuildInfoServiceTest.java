package io.jenkins.plugins.collector.service;

import hudson.model.Run;
import io.jenkins.plugins.collector.data.BuildProvider;
import io.jenkins.plugins.collector.model.BuildInfo;
import io.jenkins.plugins.collector.model.TriggerEnum;
import io.jenkins.plugins.collector.model.TriggerInfo;
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
@PrepareForTest({BuildUtil.class, LeadTimeCalculate.class, RecoveryTimeCalculate.class})
public class BuildInfoServiceTest {

  private static final Long DURATION = 1L;
  private static final Long LEAD_TIME = 2L;
  private static final Long RECOVER_TIME = 3L;
  private static final Long START_TIME = 4L;
  private static final String ID = "2";
  private static final String JOB_NAME = "buildName";
  private static final String RESULT = "0";

  @Mock
  LeadTimeCalculate leadTimeCalculate;
  @Mock
  RecoveryTimeCalculate recoveryTimeCalculate;
  @Mock
  BuildProvider buildProvider;
  @InjectMocks
  private BuildInfoService buildInfoService;

  private TriggerInfo triggerInfo;
  private Run fakeRun;

  @Before
  public void setup() {
    triggerInfo = TriggerInfo.builder()
        .triggerType(TriggerEnum.SCM_TRIGGER)
        .triggeredBy("user")
        .lastCommitHash("hash")
        .build();
    fakeRun = Mockito.mock(Run.class, Answers.RETURNS_DEEP_STUBS);
    when(fakeRun.getDuration()).thenReturn(DURATION);
    when(fakeRun.getStartTimeInMillis()).thenReturn(START_TIME);
    when(fakeRun.getId()).thenReturn(ID);
    when(leadTimeCalculate.apply(fakeRun)).thenReturn(LEAD_TIME);
    when(recoveryTimeCalculate.apply(fakeRun)).thenReturn(RECOVER_TIME);
    mockStatic(BuildUtil.class);
    when(BuildUtil.getJobName(fakeRun)).thenReturn(JOB_NAME);
    when(BuildUtil.getResultValue(fakeRun)).thenReturn(RESULT);
    when(BuildUtil.getTriggerInfo(fakeRun)).thenReturn(triggerInfo);
  }

  @Test
  public void should_get_buildInfo_when_getBuildInfo_given_a_success_run() {

    BuildInfo buildInfo = buildInfoService.getBuildInfo(fakeRun);

    assertEquals(DURATION, buildInfo.getDuration());
    assertEquals(RECOVER_TIME, buildInfo.getRecoveryTime());
    assertEquals(START_TIME, buildInfo.getStartTime());
    assertEquals(LEAD_TIME, buildInfo.getLeadTime());
    assertEquals(JOB_NAME, buildInfo.getJenkinsJob());
    assertEquals(RESULT, buildInfo.getResult());
    assertEquals(ID, buildInfo.getId());
    assertEquals(triggerInfo, buildInfo.getTriggerInfo());

  }

  @Test
  public void should_return_all_buildInfo_when_get_all_buildInfo_given_builds_need_to_handle() {
    List<Run> buildsNeedToHandle = new LinkedList<>();
    buildsNeedToHandle.add(fakeRun);
    when(buildProvider.getNeedToHandleBuilds()).thenReturn(buildsNeedToHandle);

    List<BuildInfo> allBuildInfo = buildInfoService.getAllBuildInfo();

    assertEquals(DURATION, allBuildInfo.get(0).getDuration());
    assertEquals(RECOVER_TIME, allBuildInfo.get(0).getRecoveryTime());
    assertEquals(START_TIME, allBuildInfo.get(0).getStartTime());
    assertEquals(LEAD_TIME, allBuildInfo.get(0).getLeadTime());
    assertEquals(JOB_NAME, allBuildInfo.get(0).getJenkinsJob());
    assertEquals(RESULT, allBuildInfo.get(0).getResult());
    assertEquals(ID, allBuildInfo.get(0).getId());
    assertEquals(triggerInfo, allBuildInfo.get(0).getTriggerInfo());
    Mockito.verify(buildProvider, times(1)).remove(fakeRun);
  }
}
