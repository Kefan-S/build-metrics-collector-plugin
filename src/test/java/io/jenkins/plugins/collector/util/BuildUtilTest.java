package io.jenkins.plugins.collector.util;

import hudson.model.Cause;
import hudson.model.Job;
import hudson.model.Result;
import hudson.model.Run;
import hudson.triggers.SCMTrigger;
import io.jenkins.plugins.collector.builder.FakeBuild;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.util.TreeMap;

import static io.jenkins.plugins.collector.builder.FakeJob.createMockProject;
import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

class BuildUtilTest {

    private Job pipeline;

    @BeforeEach
    void setUp() {
        pipeline = createMockProject(new TreeMap<>());
    }

    @Test
    void should_be_calculated_when_check_success_build_given_a_success_build_after_failed_build() throws IOException {
        FakeBuild previousBuild = new FakeBuild(pipeline, Result.FAILURE, 20, null);
        FakeBuild lastBuild = new FakeBuild(pipeline, Result.SUCCESS, 40, previousBuild);

        assertTrue(BuildUtil.isFirstSuccessfulBuildAfterError(lastBuild.getNextBuild(), lastBuild));
    }

    @Test
    void should_not_be_calculated_when_check_success_build_given_a_success_build_before_failed_build() throws IOException {
        FakeBuild previousBuild = new FakeBuild(pipeline, Result.SUCCESS, 40, null);
        FakeBuild lastBuild = new FakeBuild(pipeline, Result.FAILURE, 20, previousBuild);
        assertFalse(BuildUtil.isFirstSuccessfulBuildAfterError(lastBuild, previousBuild));
    }


    @Test
    void should_not_be_calculated_when_check_success_build_given_a_success_build_before_success_build() throws IOException {
        FakeBuild previousBuild = new FakeBuild(pipeline, Result.SUCCESS, 40, null);
        FakeBuild lastBuild = new FakeBuild(pipeline, Result.SUCCESS, 20, previousBuild);

        assertFalse(BuildUtil.isFirstSuccessfulBuildAfterError(lastBuild, previousBuild));
    }


    @Test
    void should_not_be_calculated_when_check_success_build_given_a_success_build_before_a_running_build() throws IOException {
        FakeBuild previousBuild = new FakeBuild(pipeline, Result.SUCCESS, 20, null);
        FakeBuild lastBuild = new FakeBuild(pipeline, null, 40, previousBuild);

        assertTrue(BuildUtil.isFirstSuccessfulBuildAfterError(lastBuild, previousBuild));
    }

    @Test
    void is_complete_over_time_given_previous_build_is_running_after_next_build() throws IOException {
        FakeBuild previousBuild = new FakeBuild(pipeline, null, 40, null);
        FakeBuild lastBuild = new FakeBuild(pipeline, Result.FAILURE, 20, previousBuild);

        assertTrue(BuildUtil.isCompleteOvertime(previousBuild, lastBuild));
    }


    @Test
    void is_complete_over_time_given_previous_build_complete_after_next_build() throws IOException {
        FakeBuild previousBuild = new FakeBuild(pipeline, Result.SUCCESS, 40, null);
        FakeBuild lastBuild = new FakeBuild(pipeline, Result.SUCCESS, 20, previousBuild);

        assertTrue(BuildUtil.isCompleteOvertime(previousBuild, lastBuild));
    }

    @Test
    void is_not_complete_over_time_given_previous_build_complete_before_next_build() throws IOException {
        FakeBuild previousBuild = new FakeBuild(pipeline, Result.FAILURE, 20, null);
        FakeBuild lastBuild = new FakeBuild(pipeline, Result.SUCCESS, 40, previousBuild);

        assertFalse(BuildUtil.isCompleteOvertime(previousBuild, lastBuild));
    }

    @Test
    void should_return_max_value_when_calculate_end_time_given_previous_build_is_running_build() throws IOException {
        FakeBuild build = new FakeBuild(pipeline, null, 40, null);
        assertEquals(Long.MAX_VALUE, BuildUtil.getBuildEndTime(build));
    }

    @Test
    void should_get_labels_when_get_labels_given_an_successful_build() {
        Run fakeRun = Mockito.mock(Run.class, Mockito.RETURNS_DEEP_STUBS);
        when(fakeRun.getParent().getFullName()).thenReturn("name");
        when(fakeRun.getResult()).thenReturn(Result.SUCCESS);
        when(fakeRun.getCause(Cause.UpstreamCause.class)).thenReturn(null);
        when(fakeRun.getCause(SCMTrigger.SCMTriggerCause.class)).thenReturn(new SCMTrigger.SCMTriggerCause("something"));

        String[] labels = BuildUtil.getLabels(fakeRun);

        assertArrayEquals(new String[]{"name", "SCM", "SUCCESS"}, labels);
    }

    @Test
    void should_get_labels_when_get_labels_given_an_running_build() {
        Run fakeRun = Mockito.mock(Run.class, Mockito.RETURNS_DEEP_STUBS);
        when(fakeRun.getParent().getFullName()).thenReturn("name");
        when(fakeRun.getResult()).thenReturn(null);
        when(fakeRun.getCause(Cause.UpstreamCause.class)).thenReturn(null);
        when(fakeRun.getCause(SCMTrigger.SCMTriggerCause.class)).thenReturn(new SCMTrigger.SCMTriggerCause("something"));

        String[] labels = BuildUtil.getLabels(fakeRun);

        assertArrayEquals(new String[]{"name", "SCM", "RUNNING"}, labels);
    }

    @Test
    void should_return_user_id_when_get_trigger_given_user_triggered_build() {
        Run fakeRun = Mockito.mock(Run.class);

        Cause.UserIdCause userIdCause = new Cause.UserIdCause("user-id");
        when(fakeRun.getCause(Cause.UpstreamCause.class)).thenReturn(null);
        when(fakeRun.getCause(SCMTrigger.SCMTriggerCause.class)).thenReturn(null);
        when(fakeRun.getCause(Cause.UserIdCause.class)).thenReturn(userIdCause);

        String trigger = BuildUtil.getTrigger(fakeRun);
        assertEquals("user-id", trigger);
    }

    @Test
    void should_return_unKnown_user_when_get_trigger_given_anonymous_user_triggered_build() {
        Run fakeRun = Mockito.mock(Run.class);

        Cause.UserIdCause userIdCause = new Cause.UserIdCause(null);
        when(fakeRun.getCause(Cause.UpstreamCause.class)).thenReturn(null);
        when(fakeRun.getCause(SCMTrigger.SCMTriggerCause.class)).thenReturn(null);
        when(fakeRun.getCause(Cause.UserIdCause.class)).thenReturn(userIdCause);

        String trigger = BuildUtil.getTrigger(fakeRun);
        assertEquals("UnKnown User", trigger);
    }

    @Test
    void should_return_unKnown_when_get_trigger_given_neither_scm_nor_user_triggered_build() {
        Run fakeRun = Mockito.mock(Run.class);

        when(fakeRun.getCause(Cause.UpstreamCause.class)).thenReturn(null);
        when(fakeRun.getCause(SCMTrigger.SCMTriggerCause.class)).thenReturn(null);
        when(fakeRun.getCause(Cause.UserIdCause.class)).thenReturn(null);

        String trigger = BuildUtil.getTrigger(fakeRun);
        assertEquals("UnKnown", trigger);
    }
}