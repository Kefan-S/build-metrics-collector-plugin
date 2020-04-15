package io.jenkins.plugins.collector.handler;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import hudson.model.Result;
import hudson.model.Run;
import io.jenkins.plugins.collector.util.BuildUtil;
import io.prometheus.client.Gauge;
import io.prometheus.client.SimpleCollector;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import javax.annotation.Nonnull;

import static io.jenkins.plugins.collector.util.BuildUtil.getBuildEndTime;
import static io.jenkins.plugins.collector.util.BuildUtil.getLabels;
import static io.jenkins.plugins.collector.util.BuildUtil.isAbortBuild;
import static io.jenkins.plugins.collector.util.BuildUtil.isCompleteOvertime;
import static java.util.Collections.singletonList;

public class RecoverTimeHandler implements Function<Run, List<SimpleCollector>> {

  private Gauge recoverTimeMetrics;

  @Inject
  public RecoverTimeHandler(@Named("recoverTimeGauge") Gauge recoverTimeMetrics) {
    this.recoverTimeMetrics = recoverTimeMetrics;
  }

  @Override
  public List<SimpleCollector> apply(@Nonnull Run successBuild) {
    Optional.of(successBuild)
        .filter(BuildUtil::isFirstSuccessfulBuildAfterError)
        .map(firstSuccessBuild -> calculateRecoverTime(firstSuccessBuild.getPreviousBuild(), firstSuccessBuild))
        .filter(recoverTime -> recoverTime > 0)
        .ifPresent(setRecoverTimeThenPush(getLabels(successBuild)));
    return singletonList(recoverTimeMetrics);
  }

  private Consumer<Long> setRecoverTimeThenPush(String... labels) {
    return recoverTime -> recoverTimeMetrics.labels(labels).set(recoverTime);
  }

  Long calculateRecoverTime(Run matchedBuild, Run currentBuild) {
    long recoverTime = Long.MIN_VALUE;
    while (!isASuccessAndFinishedMatchedBuild(matchedBuild, currentBuild)) {
      if (!isAbortBuild(matchedBuild)) {
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
