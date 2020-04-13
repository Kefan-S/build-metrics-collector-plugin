package io.jenkins.plugins.collector.data;

import hudson.model.Item;
import hudson.model.Job;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import jenkins.model.Jenkins;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(MockitoJUnitRunner.class)
public class JobProviderTest {

  @Mock
  private Jenkins jenkins;
  @InjectMocks
  private JobProvider jobProvider;

  @Test
  public void should_filter_all_empty_items_when_get_all_jobs_given_jobs_are_empty() {

    Item mockItemOne = mock(Item.class);
    Item mockItemTwo = mock(Item.class);
    List<Item> mockItems = Arrays.asList(mockItemOne, mockItemTwo);
    when(jenkins.getAllItems()).thenReturn(mockItems);

    when(mockItemOne.getAllJobs()).thenReturn(Collections.emptyList());
    when(mockItemTwo.getAllJobs()).thenReturn(Collections.emptyList());

    List<Job> result = jobProvider.getAllJobs();
    assertEquals(0, result.size());
  }

  @Test
  public void should_filter_all_empty_items_when_get_all_jobs_given_jobs_exist() {

    Item mockItemOne = mock(Item.class);
    Item mockItemTwo = mock(Item.class);
    List<Item> mockItems = Arrays.asList(mockItemOne, mockItemTwo);
    when(jenkins.getAllItems()).thenReturn(mockItems);

    Job mockJob = mock(Job.class);
    Collection mockJobs = new ArrayList();
    mockJobs.add(mockJob);
    when(mockItemOne.getAllJobs()).thenReturn(mockJobs);
    when(mockItemTwo.getAllJobs()).thenReturn(Collections.emptyList());

    List<Job> result = jobProvider.getAllJobs();
    assertEquals(new ArrayList(Arrays.asList(mockJob)), result);
  }

  @Test
  public void should_filter_all_empty_items_when_get_all_jobs_given_items_absent() {

    when(jenkins.getAllItems()).thenReturn(Collections.emptyList());

    List<Job> result = jobProvider.getAllJobs();
    assertEquals(0, result.size());
  }
}