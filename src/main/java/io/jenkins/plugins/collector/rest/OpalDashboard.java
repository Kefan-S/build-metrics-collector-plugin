package io.jenkins.plugins.collector.rest;

import com.google.inject.Inject;
import hudson.Extension;
import hudson.model.AbstractItem;
import hudson.model.RootAction;
import hudson.util.HttpResponses;
import io.jenkins.plugins.collector.config.CollectableBuildsJobProperty;
import io.jenkins.plugins.collector.data.JobProvider;
import io.jenkins.plugins.collector.service.JenkinsService;
import io.jenkins.plugins.collector.util.HttpResponseUtil;
import java.util.List;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.StaplerRequest;

import static java.util.stream.Collectors.toList;

@Extension
public class OpalDashboard implements RootAction {

  private JenkinsService jenkinsService;
  private JobProvider jobProvider;

  @Inject
  public void setJenkinsService(JenkinsService jenkinsService) {
    this.jenkinsService = jenkinsService;
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
      return HttpResponseUtil.generateHttpResponse(jenkinsService.getMetricsData(request));
    }
    if (request.getRestOfPath().equalsIgnoreCase("/users")) {
      return HttpResponseUtil.generateHttpResponse(jenkinsService.getBuildUsers(request.getParameter("jobName")));
    }
    if (request.getRestOfPath().equalsIgnoreCase("/jobs")) {
      return HttpResponseUtil.generateHttpResponse(getMonitoredJobName1());
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

  public List<String> getMonitoredJobName1() {
    return jobProvider.getAllJobs().stream()
        .filter(job -> job.getProperty(CollectableBuildsJobProperty.class) != null)
        .map(AbstractItem::getFullName)
        .map(jobName -> String.format("%s", jobName))
        .collect(toList());
  }
}
