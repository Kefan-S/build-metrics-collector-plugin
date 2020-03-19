package io.jenkins.plugins.collector.util;

import hudson.model.Item;
import hudson.model.Job;
import hudson.model.Run;
import jenkins.model.Jenkins;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;

public class Jobs {

  public static void forEachJob(Consumer<Job> consumer) {

    Jenkins jenkins = Jenkins.getInstanceOrNull();
    if (jenkins == null) {
      return;
    }
    List<Item> items = jenkins.getAllItems();
    if (items != null) {
      for (Item item : items) {
        Collection<? extends Job> jobs = item.getAllJobs();
        if (jobs != null) {
          for (Job job : jobs) {
            if (job != null) {
              consumer.accept(job);
            }
          }
        }
      }
    }
  }
}
