package io.jenkins.plugins.collector.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import hudson.model.AbstractItem;
import hudson.model.Action;
import hudson.model.Job;
import hudson.util.HttpResponses;
import io.jenkins.plugins.collector.config.CollectableBuildsJobProperty;
import io.jenkins.plugins.collector.consumer.jenkins.JenkinsMetrics;
import io.jenkins.plugins.collector.data.JobProvider;
import io.jenkins.plugins.collector.model.JenkinsFilterParameter;
import java.util.List;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.StaplerProxy;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import static java.util.stream.Collectors.toList;

public class OpalJobDashboard implements Action, StaplerProxy {

  private Job job;
  private JenkinsMetrics jenkinsMetrics;
  private JobProvider jobProvider;

  public OpalJobDashboard(Job job, JenkinsMetrics jenkinsMetrics, JobProvider jobProvider) {
    this.job = job;
    this.jenkinsMetrics = jenkinsMetrics;
    this.jobProvider = jobProvider;
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

  public HttpResponse doDynamic(StaplerRequest request) {
    if (request.getRestOfPath().equalsIgnoreCase("/data")) {
      JenkinsFilterParameter jenkinsFilterParameter = JenkinsFilterParameter.builder()
          .jobName(request.getParameter("jobName"))
          .beginTime(request.getParameter("beginTime"))
          .endTime(request.getParameter("endTime"))
          .build();
      return jenkinsResponse(jenkinsFilterParameter);
    }
    return HttpResponses.notFound();
  }

  public List<String> getMonitoredJobName() {
    return jobProvider.getAllJobs().stream()
        .filter(job -> job.getProperty(CollectableBuildsJobProperty.class) != null)
        .map(AbstractItem::getFullName)
        .map(jobName -> String.format("'%s'", jobName))
        .collect(toList());
  }

  private HttpResponse jenkinsResponse(JenkinsFilterParameter jenkinsFilterParameter) {
    return (StaplerRequest request, StaplerResponse response, Object node) -> {
      response.setStatus(StaplerResponse.SC_OK);
      response.setContentType("application/json; charset=UTF-8");
      response.addHeader("Cache-Control", "must-revalidate,no-cache,no-store");
      response.addHeader("Access-Control-Allow-Origin", "*");
      response.getWriter().write(new ObjectMapper().writeValueAsString(jenkinsMetrics.getMetrics(jenkinsFilterParameter)));
    };
  }
}
