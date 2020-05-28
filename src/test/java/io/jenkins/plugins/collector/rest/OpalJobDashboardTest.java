package io.jenkins.plugins.collector.rest;

import hudson.model.Job;
import hudson.model.JobProperty;
import io.jenkins.plugins.collector.config.CollectableBuildsJobProperty;
import io.jenkins.plugins.collector.service.JenkinsService;
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

@RunWith(PowerMockRunner.class)
@PrepareForTest(HttpResponses.class)
public class OpalJobDashboardTest {

  @Mock
  private JenkinsService jenkinsService;

  @Mock
  private StaplerRequest staplerRequest;

  @Mock
  private Job job;

  private OpalJobDashboard opalJobDashboard;

  @Before
  public void setUp() {
    opalJobDashboard = new OpalJobDashboard(job, jenkinsService);
  }

  @Test
  public void should_get_build_info_response_given_data_rest_path() {

    Mockito.when(staplerRequest.getRestOfPath()).thenReturn("/data");

    opalJobDashboard.doDynamic(staplerRequest);
    Mockito.verify(jenkinsService, Mockito.times(1)).getMetricsData(staplerRequest);
  }

  @Test
  public void should_get_users_response_given_users_rest_path() {

    Mockito.when(staplerRequest.getRestOfPath()).thenReturn("/users");
    Mockito.when(job.getName()).thenReturn("MockJob");

    opalJobDashboard.doDynamic(staplerRequest);
    Mockito.verify(jenkinsService, Mockito.times(1)).getBuildUsers("MockJob");
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