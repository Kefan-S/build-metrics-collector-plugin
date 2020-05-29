package io.jenkins.plugins.collector.rest;

import hudson.model.Job;
import hudson.model.JobProperty;
import io.jenkins.plugins.collector.config.CollectableBuildsJobProperty;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kohsuke.stapler.HttpResponses;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest(HttpResponses.class)
public class OpalJobDashboardTest {

  @Mock
  private Job job;

  private OpalJobDashboard opalJobDashboard;

  @Before
  public void setUp() {
    opalJobDashboard = new OpalJobDashboard(job);
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