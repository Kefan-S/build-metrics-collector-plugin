package io.jenkins.plugins.collector.actions;

import hudson.model.Result;
import hudson.model.Run;
import io.prometheus.client.Gauge;
import java.util.Optional;
import java.util.function.Consumer;
import javax.annotation.Nonnull;

import static io.jenkins.plugins.collector.util.BuildUtil.getBuildEndTime;
import static io.jenkins.plugins.collector.util.BuildUtil.getLabels;
import static io.jenkins.plugins.collector.util.BuildUtil.isCompleteOvertime;
import static io.jenkins.plugins.collector.util.BuildUtil.isFirstSuccessfulBuildAfterError;

public class RecoverTimeHandler implements Consumer<Run> {

  private Gauge recoverTimeMetrics;

  public RecoverTimeHandler(Gauge recoverTimeMetrics) {
    this.recoverTimeMetrics = recoverTimeMetrics;
  }

  @Override
  public void accept(@Nonnull Run successBuild) {
    Optional.of(successBuild)
        .filter(build -> isFirstSuccessfulBuildAfterError(build.getNextBuild(), build))
        .map(firstSuccessBuild -> calculateRecoverTime(firstSuccessBuild.getPreviousBuild(), firstSuccessBuild))
        .filter(recoverTime -> recoverTime > 0)
        .ifPresent(setRecoverTimeThenPush(getLabels(successBuild)));
  }

  private Consumer<Long> setRecoverTimeThenPush(String... labels) {
    return recoverTime -> recoverTimeMetrics.labels(labels).set(recoverTime);
  }

  Long calculateRecoverTime(Run matchedBuild, Run currentBuild) {
    long recoverTime = Long.MIN_VALUE;
    while (!isASuccessAndFinishedMatchedBuild(matchedBuild, currentBuild)) {
      if (!Result.ABORTED.equals(matchedBuild.getResult())) {
        recoverTime = Math.max(recoverTime, getBuildEndTime(currentBuild) - getBuildEndTime(matchedBuild));
      }
      matchedBuild = matchedBuild.getPreviousBuild();
    }
    return recoverTime;
  }

  private boolean isASuccessAndFinishedMatchedBuild(Run matchedBuild, Run currentBuild) {
    return matchedBuild == null
        || (!isCompleteOvertime(matchedBuild, currentBuild) && Result.UNSTABLE.isWorseOrEqualTo(matchedBuild.getResult()));
  }
}
