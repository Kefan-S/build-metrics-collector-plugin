package io.jenkins.plugins.collector.consumer.jenkins;

import hudson.model.TopLevelItem;
import hudson.remoting.VirtualChannel;
import io.jenkins.plugins.collector.model.BuildInfo;
import io.jenkins.plugins.collector.model.JenkinsFilterParameter;
import io.jenkins.plugins.collector.model.TriggerInfo;
import java.io.File;
import java.io.IOException;
import jenkins.model.Jenkins;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static com.google.common.collect.Lists.newArrayList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class JenkinsConsumerTest {
  @Mock
  Jenkins jenkins;
  @Mock
  VirtualChannel channel;
  @Mock
  TopLevelItem item;
  @Captor
  private ArgumentCaptor<CreateFileTask> taskArgumentCaptor;

  @Test
  public void should_call_create_task_when_call_accept_given_build_info_list() throws IOException, InterruptedException {
    BuildInfo test = BuildInfo.builder().triggerInfo(new TriggerInfo()).jenkinsJob("test").build();
    when(jenkins.getChannel()).thenReturn(channel);
    when(jenkins.getItem(test.getJenkinsJob())).thenReturn(item);
    when(item.getRootDir()).thenReturn(new File("/folder"));
    JenkinsConsumer consumer = new JenkinsConsumer(jenkins);

    consumer.accept(newArrayList(test));

    verify(channel).call(taskArgumentCaptor.capture());
    CreateFileTask task = taskArgumentCaptor.getValue();
    CreateFileTask expected = new CreateFileTask("/folder",  "null,null,null,null,test,null,null,null,null");
    assertEquals(expected, task);
  }

  @Test
  public void should_return_null_when_getMetrics_given_not_exist_job_name() {
    when(jenkins.getItem(any())).thenReturn(item);
    when(item.getRootDir()).thenReturn(new File("/folder"));
    JenkinsFilterParameter jenkinsFilterParameter = JenkinsFilterParameter
        .builder().build();

    JenkinsConsumer consumer = new JenkinsConsumer(jenkins);
    assertNull(consumer.getMetrics(jenkinsFilterParameter));

  }
}
