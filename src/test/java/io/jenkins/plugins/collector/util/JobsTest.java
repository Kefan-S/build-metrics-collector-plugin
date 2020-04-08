package io.jenkins.plugins.collector.util;

import hudson.model.Item;
import hudson.model.Job;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import jenkins.model.Jenkins;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Jenkins.class})
public class JobsTest {

  private Jenkins jenkins;
  private Consumer consumer;

  @Before
  public void setup() {
    mockStatic(Jenkins.class);
    jenkins = mock(Jenkins.class);
    consumer = mock(Consumer.class);

    when(Jenkins.getInstanceOrNull()).thenReturn(jenkins);
  }

  @Test
  public void should_filter_all_empty_items_when_forEach_given_jobs_are_empty() {

    Item mockItemOne = mock(Item.class);
    Item mockItemTwo = mock(Item.class);
    List<Item> mockItems = Arrays.asList(mockItemOne, mockItemTwo);
    when(jenkins.getAllItems()).thenReturn(mockItems);

    when(mockItemOne.getAllJobs()).thenReturn(Collections.emptyList());
    when(mockItemTwo.getAllJobs()).thenReturn(Collections.emptyList());

    Jobs.forEachJob(consumer);
    verify(consumer, never()).accept(any());
  }

  @Test
  public void should_filter_all_empty_items_when_forEach_given_jobs_exist() {

    Item mockItemOne = mock(Item.class);
    Item mockItemTwo = mock(Item.class);
    List<Item> mockItems = Arrays.asList(mockItemOne, mockItemTwo);
    when(jenkins.getAllItems()).thenReturn(mockItems);

    Job mockJob = mock(Job.class);
    Collection mockJobs = new ArrayList();
    mockJobs.add(mockJob);
    when(mockItemOne.getAllJobs()).thenReturn(mockJobs);
    when(mockItemTwo.getAllJobs()).thenReturn(Collections.emptyList());

    Jobs.forEachJob(consumer);
    verify(consumer, times(1)).accept(any());
    verify(consumer, times(1)).accept(mockJob);
  }

  @Test
  public void should_filter_all_empty_items_when_forEach_given_items_absent() {

    when(jenkins.getAllItems()).thenReturn(Collections.emptyList());

    Jobs.forEachJob(consumer);
    verify(consumer, never()).accept(any());
  }

}