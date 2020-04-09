package io.jenkins.plugins.collector.util;

import hudson.model.Cause.UpstreamCause;
import hudson.model.Job;
import jenkins.model.Jenkins;
import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class UpstreamJobGetterTest {

  @Test
  public void should_return_item_when_apply_given_jenkins_and_upstream_cause() {
    UpstreamJobGetter upstreamJobGetter = new UpstreamJobGetter();
    UpstreamCause upstreamCause = mock(UpstreamCause.class);
    Jenkins jenkins = mock(Jenkins.class);
    when(upstreamCause.getUpstreamProject()).thenReturn("some project");

    upstreamJobGetter.apply(jenkins, upstreamCause);
    verify(jenkins, times(1)).getItemByFullName("some project", Job.class);
  }
}
