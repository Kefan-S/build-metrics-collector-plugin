package io.jenkins.plugins.collector.config;

import com.google.inject.Inject;
import hudson.Extension;
import hudson.model.Action;
import hudson.model.Job;
import io.jenkins.plugins.collector.rest.OpalJobDashboard;
import io.jenkins.plugins.collector.service.JenkinsService;
import java.util.Collection;
import java.util.Collections;
import jenkins.model.TransientActionFactory;
import lombok.NonNull;

@Extension
public class OpalDashboardFactory extends TransientActionFactory<Job> {

  private JenkinsService jenkinsService;

  @Inject
  public void setJenkinsService(JenkinsService jenkinsService) {
    this.jenkinsService = jenkinsService;
  }

  @Override
  public Class<Job> type() {
    return Job.class;
  }

  @NonNull
  @Override
  public Collection<? extends Action> createFor(@NonNull Job job) {
    return Collections.singleton(new OpalJobDashboard(job, jenkinsService));
  }

}
