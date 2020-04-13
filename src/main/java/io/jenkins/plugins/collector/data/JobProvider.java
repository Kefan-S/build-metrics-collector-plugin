package io.jenkins.plugins.collector.data;

import com.google.inject.Inject;
import hudson.model.Item;
import hudson.model.Job;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import jenkins.model.Jenkins;

public class JobProvider {

  private Jenkins jenkins;

  @Inject
  public JobProvider(Jenkins jenkins) {
    this.jenkins = jenkins;
  }

  public List<Job> getAllJobs() {
    return jenkins.getAllItems()
        .stream()
        .map(Item::getAllJobs)
        .flatMap(Collection::stream)
        .collect(Collectors.toList());
  }
}
