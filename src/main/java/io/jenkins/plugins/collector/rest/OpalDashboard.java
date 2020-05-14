package io.jenkins.plugins.collector.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import hudson.Extension;
import hudson.model.AbstractItem;
import hudson.model.RootAction;
import hudson.util.HttpResponses;
import io.jenkins.plugins.collector.config.CollectableBuildsJobProperty;
import io.jenkins.plugins.collector.consumer.jenkins.JenkinsMetrics;
import io.jenkins.plugins.collector.data.JobProvider;
import java.util.List;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import static java.util.stream.Collectors.toList;

@Extension
public class OpalDashboard implements RootAction {

  private JenkinsMetrics jenkinsMetrics;
  private JobProvider jobProvider;

  @Inject
  public void setJenkinsMetrics(JenkinsMetrics jenkinsMetrics) {
    this.jenkinsMetrics = jenkinsMetrics;
  }

  @Inject
  public void setJobProvider(JobProvider jobProvider) {
    this.jobProvider = jobProvider;
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
    if (request.getRestOfPath().equalsIgnoreCase("/data")) {
      return jenkinsResponse(request.getParameter("jobName"));
    }
    return HttpResponses.notFound();
  }

  public List<String> getMonitoredJobName(){
    return jobProvider.getAllJobs().stream()
        .filter(job -> job.getProperty(CollectableBuildsJobProperty.class) != null)
        .map(AbstractItem::getFullName)
        .collect(toList());
  }

  private HttpResponse jenkinsResponse(String jobName) {
    return (StaplerRequest request, StaplerResponse response, Object node) -> {
      response.setStatus(StaplerResponse.SC_OK);
      response.setContentType("application/json; charset=UTF-8");
      response.addHeader("Cache-Control", "must-revalidate,no-cache,no-store");
      response.addHeader("Access-Control-Allow-Origin", "*");
      response.getWriter().write(new ObjectMapper().writeValueAsString(jenkinsMetrics.getMetrics(jobName)));
    };
  }
}
