package io.jenkins.plugins.collector.actions.gauge;

import hudson.model.Result;
import hudson.model.Run;
import io.prometheus.client.Gauge;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static io.jenkins.plugins.collector.util.BuildUtil.getBuildEndTime;
import static io.jenkins.plugins.collector.util.BuildUtil.isCompleteOvertime;
import static io.jenkins.plugins.collector.util.BuildUtil.isFirstSuccessfulBuildAfterError;

public class RecoverTimeHandler implements BiConsumer<String[], Run> {

  private Gauge recoverTimeMetrics;

  public RecoverTimeHandler(Gauge recoverTimeMetrics) {
    this.recoverTimeMetrics = recoverTimeMetrics;
  }

  @Override
  public void accept(String[] labels, Run successBuilds) {
    Optional.of(successBuilds)
        .filter(successBuild -> isFirstSuccessfulBuildAfterError(successBuild.getNextBuild(), successBuild))
        .map(successBuild -> calculateRecoverTime(successBuild.getPreviousBuild(), successBuild))
        .filter(recoverTime -> recoverTime > 0)
        .ifPresent(setRecoverTimeThenPush(labels));
  }

  private Consumer<Long> setRecoverTimeThenPush(String... labels) {
    return recoverTime ->
        recoverTimeMetrics.labels(labels).set(recoverTime);
  }

  Long calculateRecoverTime(Run matchedBuild, Run currentBuild) {
    if (isASuccessAndFinishedMatchedBuild(matchedBuild, currentBuild)) {
      return Long.MIN_VALUE;
    }

    if (Result.ABORTED.equals(matchedBuild.getResult())) {
      return calculateRecoverTime(matchedBuild.getPreviousBuild(), currentBuild);
    }

    return Math.max(calculateRecoverTime(matchedBuild.getPreviousBuild(), currentBuild),
        getBuildEndTime(currentBuild) - getBuildEndTime(matchedBuild));
  }

  private boolean isASuccessAndFinishedMatchedBuild(Run matchedBuild, Run currentBuild) {
    return matchedBuild == null
        || (!isCompleteOvertime(matchedBuild, currentBuild) && Result.UNSTABLE.isWorseOrEqualTo(matchedBuild.getResult()));
  }
}
