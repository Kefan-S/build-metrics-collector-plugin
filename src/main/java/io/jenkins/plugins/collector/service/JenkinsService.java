package io.jenkins.plugins.collector.service;

import com.google.inject.Inject;
import io.jenkins.plugins.collector.consumer.jenkins.JenkinsMetrics;
import io.jenkins.plugins.collector.model.BuildInfoResponse;
import io.jenkins.plugins.collector.model.JenkinsFilterParameter;
import java.util.List;
import org.kohsuke.stapler.StaplerRequest;

public class JenkinsService {

  private JenkinsMetrics jenkinsMetrics;

  @Inject
  public void setJenkinsMetrics(JenkinsMetrics jenkinsMetrics) {
    this.jenkinsMetrics = jenkinsMetrics;
  }


  public List<String> getBuildUsers(String jobName) {
    return jenkinsMetrics.getBuildUsers(jobName);
  }

  public BuildInfoResponse getMetricsData(StaplerRequest request) {
    JenkinsFilterParameter jenkinsFilterParameter = JenkinsFilterParameter.builder()
        .jobName(request.getParameter("jobName"))
        .beginTime(request.getParameter("beginTime"))
        .endTime(request.getParameter("endTime"))
        .triggerBy(request.getParameter("triggerBy"))
        .build();
    return jenkinsMetrics.getMetrics(jenkinsFilterParameter);
  }
}
