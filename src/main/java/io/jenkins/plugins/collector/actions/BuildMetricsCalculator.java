package io.jenkins.plugins.collector.actions;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import hudson.model.Run;

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
    private static BiConsumer<String, Run> successBuildHandler;

    public static void handleBuild(String label, Run build) {
        if (Objects.isNull(build) || isAbortBuild(build)) {
            return;
        }

        if (isSuccessfulBuild(build)) {
            successBuildHandler.accept(label, build);
        }

        buildInfoHandler.accept(label, build);
    }
}
