package io.jenkins.plugins.collector.util;

import hudson.model.Job;
import hudson.model.Result;
import io.jenkins.plugins.collector.builder.FakeBuild;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.TreeMap;

import static io.jenkins.plugins.collector.builder.FakeJob.createMockProject;
import static org.junit.Assert.*;

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
}