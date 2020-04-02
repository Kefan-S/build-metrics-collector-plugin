package io.jenkins.plugins.collector.actions;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import hudson.model.Run;
import io.jenkins.plugins.collector.util.BuildUtil;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

import static io.jenkins.plugins.collector.util.BuildUtil.isSuccessfulBuild;

public class BuildMetricsCalculator {
    @Inject
    @Named("buildInfoHandlerSupplier")
    private static Supplier<BiConsumer<String[], Run>> buildInfoHandler;

    @Inject
    @Named("successBuildHandlerSupplier")
    private static Supplier<BiConsumer<String[], Run>> successBuildHandler;

    public static void handleBuild(Run build) {
        if (Objects.isNull(build)) {
            return;
        }

        String[] labels = BuildUtil.getLabels(build);
        if (isSuccessfulBuild(build)) {
            successBuildHandler.get().accept(labels, build);
        }

        buildInfoHandler.get().accept(labels, build);
    }
}
