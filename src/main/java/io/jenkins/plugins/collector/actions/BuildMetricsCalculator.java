package io.jenkins.plugins.collector.actions;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import hudson.model.Run;
import io.jenkins.plugins.collector.util.BuildUtil;

import java.util.Objects;
import java.util.function.BiConsumer;

import static io.jenkins.plugins.collector.util.BuildUtil.isAbortBuild;
import static io.jenkins.plugins.collector.util.BuildUtil.isSuccessfulBuild;

public class BuildMetricsCalculator {
    @Inject
    @Named("buildInfoHandler")
    private static BiConsumer buildInfoHandler;

    @Inject
    @Named("successBuildHandler")
    private static BiConsumer<String[], Run> successBuildHandler;

    public static void handleBuild(Run build) {
        if (Objects.isNull(build) || isAbortBuild(build)) {
            return;
        }

        String[] labels = BuildUtil.getLabels(build);
        if (isSuccessfulBuild(build)) {
            successBuildHandler.accept(labels, build);
        }

        buildInfoHandler.accept(labels, build);
    }
}
