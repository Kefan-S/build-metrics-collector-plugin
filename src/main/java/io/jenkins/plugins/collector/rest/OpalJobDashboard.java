package io.jenkins.plugins.collector.rest;

import hudson.model.Action;
import hudson.model.Job;
import io.jenkins.plugins.collector.config.CollectableBuildsJobProperty;
import org.kohsuke.stapler.StaplerProxy;

public class OpalJobDashboard implements Action, StaplerProxy {

  private Job job;

  public OpalJobDashboard(Job job) {
    this.job = job;
  }

  public int getBuildStepsCount() {
    return job.getNextBuildNumber();
  }

  public boolean getPostBuildStepsCount() {
    return job.getProperty(CollectableBuildsJobProperty.class) != null;
  }

  @Override
  public String getIconFileName() {
    return this.job.getProperty(CollectableBuildsJobProperty.class) != null ? "/plugin/build-metrics-collector-plugin/images/opal.png" : null;
  }

  @Override
  public String getDisplayName() {
    return "Opal";
  }

  @Override
  public String getUrlName() {
    return "opal";
  }

  @Override
  public Object getTarget() {
    return this.job.getProperty(CollectableBuildsJobProperty.class) != null ? this : null;
  }
}
