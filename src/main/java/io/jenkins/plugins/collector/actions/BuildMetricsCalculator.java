package io.jenkins.plugins.collector.actions;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import hudson.model.Run;
import io.jenkins.plugins.collector.util.BuildUtil;
import java.util.Objects;
import java.util.function.BiConsumer;

import static io.jenkins.plugins.collector.util.BuildUtil.isSuccessfulBuild;

public class BuildMetricsCalculator {

  @Inject
  @Named("buildInfoHandlerSupplier")
  private static BiConsumer<String[], Run> buildInfoHandler;

  @Inject
  @Named("successBuildHandlerSupplier")
  private static BiConsumer<String[], Run> successBuildHandler;

  public static void handleBuild(Run build) {
    if (Objects.isNull(build)) {
      return;
    }

    String[] labels = BuildUtil.getLabels(build);
    if (isSuccessfulBuild(build)) {
      successBuildHandler.accept(labels, build);
    }

    buildInfoHandler.accept(labels, build);
  }
}
