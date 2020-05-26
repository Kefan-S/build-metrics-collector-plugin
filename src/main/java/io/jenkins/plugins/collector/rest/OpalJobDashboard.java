package io.jenkins.plugins.collector.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import hudson.model.Action;
import hudson.model.Job;
import hudson.util.HttpResponses;
import io.jenkins.plugins.collector.config.CollectableBuildsJobProperty;
import io.jenkins.plugins.collector.consumer.jenkins.JenkinsMetrics;
import io.jenkins.plugins.collector.model.BuildInfoResponse;
import io.jenkins.plugins.collector.model.JenkinsFilterParameter;
import java.util.List;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.StaplerProxy;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

public class OpalJobDashboard implements Action, StaplerProxy {

  private Job job;
  private JenkinsMetrics jenkinsMetrics;

  public OpalJobDashboard(Job job, JenkinsMetrics jenkinsMetrics) {
    this.job = job;
    this.jenkinsMetrics = jenkinsMetrics;
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
      JenkinsFilterParameter jenkinsFilterParameter = JenkinsFilterParameter.builder()
          .jobName(request.getParameter("jobName"))
          .beginTime(request.getParameter("beginTime"))
          .endTime(request.getParameter("endTime"))
          .build();
      return jenkinsResponse(jenkinsFilterParameter);
    }
    if (request.getRestOfPath().equalsIgnoreCase("/users")) {
      return usersResponse(job.getName());
    }
    return HttpResponses.notFound();
  }

  private HttpResponse usersResponse(String jobName) {
    List<String> buildUsers = jenkinsMetrics.getBuildUsers(jobName);
    return generateHttpResponse(buildUsers);
  }

  private HttpResponse jenkinsResponse(JenkinsFilterParameter jenkinsFilterParameter) {
    BuildInfoResponse buildInfoResponse = jenkinsMetrics.getMetrics(jenkinsFilterParameter);
    return generateHttpResponse(buildInfoResponse);
  }

  private HttpResponse generateHttpResponse(Object responseBody) {
    return (StaplerRequest request, StaplerResponse response, Object node) -> {
      response.setStatus(StaplerResponse.SC_OK);
      response.setContentType("application/json; charset=UTF-8");
      response.addHeader("Access-Control-Allow-Origin", "*");
      response.addHeader("Cache-Control", "must-revalidate,no-cache,no-store");
      response.getWriter().write(new ObjectMapper().writeValueAsString(responseBody));
    };
  }
}
