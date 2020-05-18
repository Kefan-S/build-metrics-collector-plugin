package io.jenkins.plugins.collector.consumer.jenkins;

import io.jenkins.plugins.collector.model.BuildInfo;
import io.jenkins.plugins.collector.model.BuildInfoResponse;
import io.jenkins.plugins.collector.model.JenkinsFilterParameter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class JenkinsAdapterTest {

  public static final Long BEGIN_TIME = 1588809600L;
  public static final Long END_TIME = 1588982400L;
  private JenkinsAdapter jenkinsAdapter = new JenkinsAdapter();
  private List<BuildInfo> buildInfos;
  private JenkinsFilterParameter jenkinsFilterParameter;

  @Before
  public void setUp() {

    buildInfos = new ArrayList<>();
    jenkinsFilterParameter = JenkinsFilterParameter
        .builder().jobName("test").endTime(String.valueOf(END_TIME)).beginTime(String.valueOf(BEGIN_TIME)).build();
  }

  @Test
  public void should_ignore_aborted_build_given_a_aborted_buildInfo() {
    BuildInfo abortedBuildInfo = buildFailedBuildInfo();
    abortedBuildInfo.setResult("4");
    buildInfos.add(abortedBuildInfo);

    assertNull(jenkinsAdapter.adapt(buildInfos, jenkinsFilterParameter));
  }

  @Test
  public void should_return_buildInfoResponse_given_buildInfoList_with_two_valid_build() {
    BuildInfo buildInfo = buildSuccessfulBuildInfo();
    BuildInfo failedBuildInfo = buildFailedBuildInfo();

    buildInfos.add(buildInfo);
    buildInfos.add(failedBuildInfo);
    buildInfos.add(failedBuildInfo);

    BuildInfoResponse response = jenkinsAdapter.adapt(buildInfos, jenkinsFilterParameter);
    assertEquals(Integer.valueOf(3), response.getDeploymentFrequency());
    assertEquals(new BigDecimal("0.6666"), response.getFailureRate());
    assertEquals(Arrays.asList(1L, null, null), response.getLeadTime());
    assertEquals(Arrays.asList(1L, null, null), response.getLeadTime());
    assertEquals(Arrays.asList(BEGIN_TIME, END_TIME, END_TIME), response.getStartTime());
    assertEquals(Arrays.asList(1L, 2L, 2L), response.getDuration());
  }

  @Test
  public void should_return_null_given_buildsInfoList_with_startTime_not_in_the_filter_range() {
    BuildInfo buildInfo = buildSuccessfulBuildInfo();
    buildInfo.setStartTime(0L);
    buildInfos.add(buildInfo);

    assertNull(jenkinsAdapter.adapt(buildInfos, jenkinsFilterParameter));
  }

  private BuildInfo buildSuccessfulBuildInfo() {
    return BuildInfo.builder()
        .duration(1L)
        .leadTime(1L)
        .recoverTime(1L)
        .startTime(BEGIN_TIME)
        .jenkinsJob("name")
        .result("0")
        .build();
  }

  private BuildInfo buildFailedBuildInfo() {
    return BuildInfo.builder()
        .duration(2L)
        .startTime(END_TIME)
        .jenkinsJob("name")
        .result("2")
        .build();
  }
}
