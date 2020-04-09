package io.jenkins.plugins.collector.actions;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import hudson.model.Run;
import io.jenkins.plugins.collector.util.BuildUtil;
import java.util.Objects;
import java.util.function.BiConsumer;

import static io.jenkins.plugins.collector.util.BuildUtil.isSuccessfulBuild;

public class BuildMetricsCalculator {

  private static BiConsumer<String[], Run> buildInfoHandler;

  private static BiConsumer<String[], Run> successBuildHandler;

  @Inject
  BuildMetricsCalculator(@Named("buildInfoHandler") BiConsumer<String[], Run> buildInfoHandler, @Named("successBuildHandler") BiConsumer<String[], Run> successBuildHandler) {
    this.buildInfoHandler = buildInfoHandler;
    this.successBuildHandler = successBuildHandler;
  }

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
