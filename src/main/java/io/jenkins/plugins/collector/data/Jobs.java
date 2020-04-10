package io.jenkins.plugins.collector.data;

import hudson.model.Item;
import hudson.model.Job;
import java.util.Collection;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;
import jenkins.model.Jenkins;

public class Jobs {

  public static void forEachJob(Consumer<Job> consumer) {
    Optional.ofNullable(Jenkins.getInstanceOrNull())
        .map(Jenkins::getAllItems)
        .map(Collection::stream)
        .orElse(Stream.empty())
        .map(Item::getAllJobs)
        .flatMap(Collection::stream)
        .forEach(consumer);
  }
}