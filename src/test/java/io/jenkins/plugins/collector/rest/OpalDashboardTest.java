package io.jenkins.plugins.collector.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import hudson.model.Job;
import hudson.model.JobProperty;
import io.jenkins.plugins.collector.config.CollectableBuildsJobProperty;
import io.jenkins.plugins.collector.consumer.jenkins.JenkinsMetrics;
import io.jenkins.plugins.collector.data.JobProvider;
import io.jenkins.plugins.collector.model.BuildInfoResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;
import javax.servlet.ServletException;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.HttpResponses;
import org.kohsuke.stapler.HttpResponses.HttpResponseException;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.junit.Assert.assertEquals;


@RunWith(PowerMockRunner.class)
@PrepareForTest(HttpResponses.class)
public class OpalDashboardTest {

  @Test
  public void should_get_build_info_response_given_data_rest_path() throws IOException, ServletException {
    OpalDashboard opalDashboard = new OpalDashboard();
    JenkinsMetrics jenkinsMetrics = Mockito.mock(JenkinsMetrics.class);
    BuildInfoResponse buildInfoResponse = BuildInfoResponse.builder().build();
    Mockito.when(jenkinsMetrics.getMetrics("MockJob")).thenReturn(buildInfoResponse);
    opalDashboard.setJenkinsMetrics(jenkinsMetrics);
    StaplerRequest staplerRequest = Mockito.mock(StaplerRequest.class);
    Mockito.when(staplerRequest.getParameter("jobName")).thenReturn("MockJob");
    Mockito.when(staplerRequest.getRestOfPath()).thenReturn("/data");
    HttpResponse httpResponse = opalDashboard.doDynamic(staplerRequest);
    StaplerResponse staplerResponse = Mockito.mock(StaplerResponse.class);
    PrintWriter printWriter = Mockito.mock(PrintWriter.class);
    Mockito.when(staplerResponse.getWriter()).thenReturn(printWriter);
    httpResponse.generateResponse(null, staplerResponse, null);
    Mockito.verify(printWriter, Mockito.times(1)).write(new ObjectMapper().writeValueAsString(buildInfoResponse));
  }

  @Test
  public void should_get_not_found_response_given_wrong_rest_path() {
    OpalDashboard opalDashboard = new OpalDashboard();
    StaplerRequest staplerRequest = Mockito.mock(StaplerRequest.class);
    Mockito.when(staplerRequest.getRestOfPath()).thenReturn("/wrongPath");
    HttpResponseException httpResponseException = HttpResponses.notFound();
    PowerMockito.mockStatic(HttpResponses.class);
    PowerMockito.when(HttpResponses.notFound()).thenReturn(httpResponseException);
    HttpResponse httpResponse = opalDashboard.doDynamic(staplerRequest);
    Assert.assertEquals(httpResponse, httpResponseException);
  }

  @Test
  public void should_get_all_job_names_monitored_by_opal() {
    Job monitoredJob = Mockito.mock(Job.class);
    Mockito.when(monitoredJob.getProperty(CollectableBuildsJobProperty.class)).thenReturn(Mockito.mock(JobProperty.class));
    Mockito.when(monitoredJob.getFullName()).thenReturn("monitoredJob");
    Job unMonitoredJob = Mockito.mock(Job.class);
    Mockito.when(unMonitoredJob.getProperty(CollectableBuildsJobProperty.class)).thenReturn(null);
    Mockito.when(unMonitoredJob.getFullName()).thenReturn("unMonitoredJob");
    OpalDashboard opalDashboard = new OpalDashboard();
    JobProvider jobProvider = Mockito.mock(JobProvider.class);
    Mockito.when(jobProvider.getAllJobs()).thenReturn(Arrays.asList(monitoredJob, unMonitoredJob));
    opalDashboard.setJobProvider(jobProvider);

    List<String> jobNames = opalDashboard.getMonitoredJobName();
    assertEquals(jobNames, Arrays.asList("monitoredJob"));
  }

}
