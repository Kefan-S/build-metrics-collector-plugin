package io.jenkins.plugins.collector.util;

import hudson.model.Cause.UpstreamCause;
import hudson.model.Job;
import java.util.function.BiFunction;
import jenkins.model.Jenkins;

public class UpstreamJobGetter implements BiFunction<Jenkins, UpstreamCause, Job> {

  @Override
  public  Job apply(Jenkins jenkins, UpstreamCause upstreamCause) {
    return jenkins.getItemByFullName(upstreamCause.getUpstreamProject(), Job.class);
  }
}

