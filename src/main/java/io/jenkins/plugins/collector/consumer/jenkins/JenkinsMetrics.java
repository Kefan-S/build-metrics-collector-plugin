package io.jenkins.plugins.collector.consumer.jenkins;

import io.jenkins.plugins.collector.model.BuildInfo;
import io.jenkins.plugins.collector.model.BuildInfoResponse;
import io.jenkins.plugins.collector.model.JenkinsFilterParameter;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public interface JenkinsMetrics extends Consumer<List<BuildInfo>> {

  BuildInfoResponse getMetrics(JenkinsFilterParameter jenkinsFilterParameter);

  Set<String> getBuildUsers(String jobName);

}

