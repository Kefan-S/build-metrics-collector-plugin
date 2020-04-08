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

public class LeadTimeHandler implements BiConsumer<String[], Run> {

  private Gauge leadTimeMetrics;

  public LeadTimeHandler(Gauge leadTimeMetrics) {
    this.leadTimeMetrics = leadTimeMetrics;
  }

  @Override
  public void accept(String[] labels, Run successBuild) {
    Optional.of(successBuild)
        .filter(build -> isFirstSuccessfulBuildAfterError(build.getNextBuild(), build))
        .map(firstSuccessBuild -> calculateLeadTime(firstSuccessBuild.getPreviousBuild(), firstSuccessBuild))
        .ifPresent(setLeadTimeThenPush(labels));
  }

  private Consumer<Long> setLeadTimeThenPush(String... labels) {
    return leadTime -> this.leadTimeMetrics.labels(labels).set(leadTime);
  }

  private Long calculateLeadTime(Run matchedBuild, Run successBuild) {
    long leadTime = successBuild.getDuration();
    while (!isASuccessAndFinishedMatchedBuild(matchedBuild, successBuild)) {
      if (!Result.ABORTED.equals(matchedBuild.getResult())) {
        leadTime = Math.max(leadTime, getBuildEndTime(successBuild) - matchedBuild.getStartTimeInMillis());
      }
      matchedBuild = matchedBuild.getPreviousBuild();
    }
    return leadTime;
  }

  private boolean isASuccessAndFinishedMatchedBuild(Run matchedBuild, Run currentBuild) {
    return matchedBuild == null
        || (!isCompleteOvertime(matchedBuild, currentBuild) && Result.UNSTABLE.isWorseOrEqualTo(matchedBuild.getResult()));
  }
}
