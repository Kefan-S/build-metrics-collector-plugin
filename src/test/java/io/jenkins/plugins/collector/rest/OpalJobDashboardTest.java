package io.jenkins.plugins.collector.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import hudson.model.Job;
import hudson.model.JobProperty;
import io.jenkins.plugins.collector.config.CollectableBuildsJobProperty;
import io.jenkins.plugins.collector.consumer.jenkins.JenkinsMetrics;
import io.jenkins.plugins.collector.model.BuildInfoResponse;
import io.jenkins.plugins.collector.model.JenkinsFilterParameter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.ServletException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.HttpResponses;
import org.kohsuke.stapler.HttpResponses.HttpResponseException;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest(HttpResponses.class)
public class OpalJobDashboardTest {

  @Mock
  private JenkinsMetrics jenkinsMetrics;

  @Mock
  private StaplerRequest staplerRequest;

  @Mock
  private StaplerResponse staplerResponse;

  @Mock
  private Job job;

  private OpalJobDashboard opalJobDashboard;

  @Before
  public void setUp() {
    opalJobDashboard = new OpalJobDashboard(job, jenkinsMetrics);
  }

  @Test
  public void should_get_build_info_response_given_data_rest_path() throws IOException, ServletException {

    PrintWriter printWriter = Mockito.mock(PrintWriter.class);
    BuildInfoResponse buildInfoResponse = BuildInfoResponse.builder().build();
    JenkinsFilterParameter jenkinsFilterParameter = JenkinsFilterParameter
        .builder().jobName("MockJob").build();

    Mockito.when(jenkinsMetrics.getMetrics(jenkinsFilterParameter)).thenReturn(buildInfoResponse);
    Mockito.when(staplerRequest.getParameter("jobName")).thenReturn("MockJob");
    Mockito.when(staplerRequest.getRestOfPath()).thenReturn("/data");

    HttpResponse httpResponse = opalJobDashboard.doDynamic(staplerRequest);

    Mockito.when(staplerResponse.getWriter()).thenReturn(printWriter);
    httpResponse.generateResponse(null, staplerResponse, null);
    Mockito.verify(printWriter, Mockito.times(1)).write(new ObjectMapper().writeValueAsString(buildInfoResponse));
  }

  @Test
  public void should_get_users_response_given_users_rest_path() throws IOException, ServletException {

    PrintWriter printWriter = Mockito.mock(PrintWriter.class);
    List<String> users = new ArrayList<>();
    users.add("trigger user1");

    Mockito.when(jenkinsMetrics.getBuildUsers("MockJob")).thenReturn(users);
    Mockito.when(job.getName()).thenReturn("MockJob");
    Mockito.when(staplerRequest.getRestOfPath()).thenReturn("/users");

    HttpResponse httpResponse = opalJobDashboard.doDynamic(staplerRequest);

    Mockito.when(staplerResponse.getWriter()).thenReturn(printWriter);
    httpResponse.generateResponse(null, staplerResponse, null);
    Mockito.verify(printWriter, Mockito.times(1)).write(new ObjectMapper().writeValueAsString(users));
  }

  @Test
  public void should_get_not_found_response_given_wrong_rest_path() {

    Mockito.when(staplerRequest.getRestOfPath()).thenReturn("/wrongPath");
    HttpResponseException httpResponseException = HttpResponses.notFound();
    PowerMockito.mockStatic(HttpResponses.class);
    PowerMockito.when(HttpResponses.notFound()).thenReturn(httpResponseException);

    HttpResponse httpResponse = opalJobDashboard.doDynamic(staplerRequest);
    Assert.assertEquals(httpResponse, httpResponseException);
  }

  @Test
  public void should_show_opal_and_return_itself_when_set_job_collect_config() {
    Mockito.when(job.getProperty(CollectableBuildsJobProperty.class)).thenReturn(Mockito.mock(JobProperty.class));
    Assert.assertEquals("/plugin/build-metrics-collector-plugin/images/opal.png", opalJobDashboard.getIconFileName());
    Assert.assertEquals(opalJobDashboard, opalJobDashboard.getTarget());
  }

  @Test
  public void should_return_null_target_and_icon_file_when_not_set_job_collect_config() {
    Mockito.when(job.getProperty(CollectableBuildsJobProperty.class)).thenReturn(null);
    Assert.assertNull(opalJobDashboard.getIconFileName());
    Assert.assertNull(opalJobDashboard.getTarget());
  }

  @Test
  public void should_return_job_name_when_get_current_job() {
    Mockito.when(job.getName()).thenReturn("MockJob");
    Assert.assertEquals("'MockJob'", opalJobDashboard.getCurrentJobName());
  }
}