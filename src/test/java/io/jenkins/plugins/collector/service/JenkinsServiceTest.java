package io.jenkins.plugins.collector.service;

import io.jenkins.plugins.collector.consumer.jenkins.JenkinsMetrics;
import io.jenkins.plugins.collector.model.BuildInfoResponse;
import io.jenkins.plugins.collector.model.JenkinsFilterParameter;
import java.util.HashSet;
import java.util.Set;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kohsuke.stapler.HttpResponses;
import org.kohsuke.stapler.StaplerRequest;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest(HttpResponses.class)
public class JenkinsServiceTest {
  @Mock
  private JenkinsMetrics jenkinsMetrics;

  @Mock
  private StaplerRequest staplerRequest;


  private JenkinsService jenkinsService;


  @Before
  public void setUp() {
    jenkinsService = new JenkinsService();
    jenkinsService.setJenkinsMetrics(jenkinsMetrics);
  }

  @Test
  public void should_return_build_info_response_when_get_metrics_data() {
    BuildInfoResponse buildInfoResponse = BuildInfoResponse.builder().build();
    JenkinsFilterParameter jenkinsFilterParameter = JenkinsFilterParameter
        .builder().jobName("MockJob").beginTime("1").endTime("2").build();

    Mockito.when(staplerRequest.getParameter("jobName")).thenReturn("MockJob");
    Mockito.when(staplerRequest.getParameter("beginTime")).thenReturn("1");
    Mockito.when(staplerRequest.getParameter("endTime")).thenReturn("2");

    Mockito.when(jenkinsMetrics.getMetrics(jenkinsFilterParameter)).thenReturn(buildInfoResponse);

    BuildInfoResponse metricsData = jenkinsService.getMetricsData(staplerRequest);
    Mockito.verify(jenkinsMetrics, Mockito.times(1)).getMetrics(jenkinsFilterParameter);
    Assert.assertEquals(buildInfoResponse, metricsData);
  }

  @Test
  public void should_return_user_list_when_get_build_users() {
    Set<String> users = new HashSet<>();
    users.add("trigger user1");
    users.add("trigger user1");
    Mockito.when(jenkinsMetrics.getBuildUsers("MockJob")).thenReturn(users);

    Set<String> actualUser = jenkinsService.getBuildUsers("MockJob");
    Assert.assertEquals(users, actualUser);
  }

}