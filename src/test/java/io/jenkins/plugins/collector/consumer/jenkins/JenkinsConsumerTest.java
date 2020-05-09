package io.jenkins.plugins.collector.consumer.jenkins;

import hudson.remoting.VirtualChannel;
import io.jenkins.plugins.collector.model.BuildInfo;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class JenkinsConsumerTest {
  @Mock
  Jenkins jenkins;
  @Mock
  VirtualChannel channel;
  @Captor
  private ArgumentCaptor<CreateFileTask> taskArgumentCaptor;

  @Test
  public void should_call_create_task_when_call_accept_given_build_info_list() throws IOException, InterruptedException {
    when(jenkins.getRootDir()).thenReturn(new File("/folder"));
    when(jenkins.getChannel()).thenReturn(channel);

    JenkinsConsumer consumer = new JenkinsConsumer(jenkins);
    consumer.accept(newArrayList(BuildInfo.builder().jenkinsJob("test").build()));

    verify(channel).call(taskArgumentCaptor.capture());
    CreateFileTask task = taskArgumentCaptor.getValue();
    CreateFileTask expected = new CreateFileTask("/folder/cache4Opal/", "test", "null,null,null,null,test,null,null,null");
    assertEquals(expected, task);
  }

  @Test
  public void should_return_null_when_get_data_stream_given_not_exist_job_name(){
    when(jenkins.getRootDir()).thenReturn(new File("/folder"));

    JenkinsConsumer consumer = new JenkinsConsumer(jenkins);
    InputStream result = consumer.getDataStream("test");

    assertNull(result);
  }
}