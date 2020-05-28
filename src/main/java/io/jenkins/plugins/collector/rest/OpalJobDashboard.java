package io.jenkins.plugins.collector.rest;

import hudson.model.Action;
import hudson.model.Job;
import hudson.util.HttpResponses;
import io.jenkins.plugins.collector.config.CollectableBuildsJobProperty;
import io.jenkins.plugins.collector.service.JenkinsService;
import io.jenkins.plugins.collector.util.HttpResponseUtil;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.StaplerProxy;
import org.kohsuke.stapler.StaplerRequest;

public class OpalJobDashboard implements Action, StaplerProxy {

  private Job job;
  private JenkinsService jenkinsService;

  public OpalJobDashboard(Job job, JenkinsService jenkinsService) {
    this.job = job;
    this.jenkinsService = jenkinsService;
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

  public String getCurrentJobName() {
    return String.format("'%s'", job.getName());
  }

  public HttpResponse doDynamic(StaplerRequest request) {
    if (request.getRestOfPath().equalsIgnoreCase("/data")) {
      return HttpResponseUtil.generateHttpResponse(jenkinsService.getMetricsData(request));
    }
    if (request.getRestOfPath().equalsIgnoreCase("/users")) {
      return HttpResponseUtil.generateHttpResponse(jenkinsService.getBuildUsers(job.getName()));
    }
    return HttpResponses.notFound();
  }

}
