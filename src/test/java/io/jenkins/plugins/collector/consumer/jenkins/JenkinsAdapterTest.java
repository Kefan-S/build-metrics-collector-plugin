package io.jenkins.plugins.collector.consumer.jenkins;

import io.jenkins.plugins.collector.model.BuildInfo;
import io.jenkins.plugins.collector.model.BuildInfoResponse;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class JenkinsAdapterTest {

  private JenkinsAdapter jenkinsAdapter = new JenkinsAdapter();
  private List<BuildInfo> buildInfos;

  @Before
  public void setUp() {
    buildInfos = new ArrayList<>();
  }

  @Test
  public void should_ignore_aborted_build_given_a_aborted_buildInfo() {
    BuildInfo abortedBuildInfo = buildFailedBuildInfo();
    abortedBuildInfo.setResult("4");
    buildInfos.add(abortedBuildInfo);

    assertNull(jenkinsAdapter.adapt(buildInfos));
  }

  @Test
  public void should_return_buildInfoResponse_given_buildInfoList_with_two_valid_build() {
    BuildInfo buildInfo = buildSuccessfulBuildInfo();
    BuildInfo failedBuildInfo = buildFailedBuildInfo();

    buildInfos.add(buildInfo);
    buildInfos.add(failedBuildInfo);

    BuildInfoResponse response = jenkinsAdapter.adapt(buildInfos);
    assertEquals(Integer.valueOf(2), response.getDeploymentFrequency());
    assertEquals(new BigDecimal("0.500"), response.getFailureRate());
    assertEquals(Arrays.asList(1L, null), response.getLeadTime());
    assertEquals(Arrays.asList(1L, null), response.getLeadTime());
    assertEquals(Arrays.asList(1L, 2L), response.getStartTime());
    assertEquals(Arrays.asList(1L, 2L), response.getDuration());
  }

  private BuildInfo buildSuccessfulBuildInfo() {
    return BuildInfo.builder()
        .duration(1L)
        .leadTime(1L)
        .recoverTime(1L)
        .startTime(1L)
        .jenkinsJob("name")
        .result("0")
        .triggeredBy("user")
        .build();
  }

  private BuildInfo buildFailedBuildInfo() {
    return BuildInfo.builder()
        .duration(2L)
        .startTime(2L)
        .jenkinsJob("name")
        .result("2")
        .triggeredBy("user")
        .build();
  }
}