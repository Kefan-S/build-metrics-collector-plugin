package io.jenkins.plugins.collector.rest;

import hudson.model.Job;
import hudson.model.JobProperty;
import io.jenkins.plugins.collector.config.CollectableBuildsJobProperty;
import io.jenkins.plugins.collector.data.JobProvider;
import io.jenkins.plugins.collector.service.JenkinsService;
import java.util.Arrays;
import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.HttpResponses;
import org.kohsuke.stapler.HttpResponses.HttpResponseException;
import org.kohsuke.stapler.StaplerRequest;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.junit.Assert.assertEquals;


@RunWith(PowerMockRunner.class)
@PrepareForTest(HttpResponses.class)
public class OpalDashboardTest {

  @Mock
  private JenkinsService jenkinsService;

  @Mock
  private StaplerRequest staplerRequest;

  private OpalDashboard opalDashboard;

  @Before
  public void setUp() {
    opalDashboard = new OpalDashboard();
    opalDashboard.setJenkinsService(jenkinsService);
  }

  @Test
  public void should_return_jenkins_response_given_data_rest_path() {
    Mockito.when(staplerRequest.getRestOfPath()).thenReturn("/data");

    opalDashboard.doDynamic(staplerRequest);
    Mockito.verify(jenkinsService, Mockito.times(1)).getMetricsData(staplerRequest);
  }

  @Test
  public void should_return_users_response_given_data_rest_path() {
    Mockito.when(staplerRequest.getRestOfPath()).thenReturn("/users");
    Mockito.when(staplerRequest.getParameter("jobName")).thenReturn("mockJob");

    opalDashboard.doDynamic(staplerRequest);
    Mockito.verify(jenkinsService, Mockito.times(1)).getBuildUsers("mockJob");
  }

  @Test
  public void should_get_not_found_response_given_wrong_rest_path() {

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
    JobProvider jobProvider = Mockito.mock(JobProvider.class);
    Mockito.when(jobProvider.getAllJobs()).thenReturn(Arrays.asList(monitoredJob, unMonitoredJob));
    opalDashboard.setJobProvider(jobProvider);

    List<String> jobNames = opalDashboard.getMonitoredJobName();
    assertEquals(jobNames, Arrays.asList("'monitoredJob'"));
  }

}
