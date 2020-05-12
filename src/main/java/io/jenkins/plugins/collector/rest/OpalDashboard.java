package io.jenkins.plugins.collector.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import hudson.Extension;
import hudson.model.RootAction;
import hudson.util.HttpResponses;
import io.jenkins.plugins.collector.consumer.jenkins.JenkinsMetrics;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

@Extension
public class OpalDashboard implements RootAction {

  private JenkinsMetrics jenkinsMetrics;

  @Inject
  public void setJenkinsMetrics(JenkinsMetrics jenkinsMetrics) {
    this.jenkinsMetrics = jenkinsMetrics;
  }

  @Override
  public String getIconFileName() {
    return "/plugin/build-metrics-collector-plugin/images/opal.png";
  }

  @Override
  public String getDisplayName() {
    return "Opal";
  }

  @Override
  public String getUrlName() {
    return "opal";
  }

  public HttpResponse doDynamic(StaplerRequest request) {
    if (request.getRestOfPath().contains("jobname")) {
      return jenkinsResponse(request.getParameter("jobname"));
    }
    return HttpResponses.notFound();
  }

  private HttpResponse jenkinsResponse(String jobName) {
    return (StaplerRequest request, StaplerResponse response, Object node) -> {
      response.setStatus(StaplerResponse.SC_OK);
      response.setContentType("application/json; charset=UTF-8");
      response.addHeader("Cache-Control", "must-revalidate,no-cache,no-store");
      response.getWriter().write(new ObjectMapper().writeValueAsString(jenkinsMetrics.getMetrics(jobName)));
    };
  }
}
