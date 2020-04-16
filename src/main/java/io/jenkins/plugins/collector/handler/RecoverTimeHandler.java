package io.jenkins.plugins.collector.handler;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import hudson.model.Result;
import hudson.model.Run;
import io.jenkins.plugins.collector.util.BuildUtil;
import io.prometheus.client.Collector.MetricFamilySamples;
import io.prometheus.client.Gauge;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import javax.annotation.Nonnull;

import static com.google.common.collect.Lists.newArrayList;
import static io.jenkins.plugins.collector.util.BuildUtil.getBuildEndTime;
import static io.jenkins.plugins.collector.util.BuildUtil.getLabels;
import static io.jenkins.plugins.collector.util.BuildUtil.isAbortBuild;
import static io.jenkins.plugins.collector.util.BuildUtil.isCompleteOvertime;
import static java.util.Collections.emptyList;

public class RecoverTimeHandler implements Function<Run, List<MetricFamilySamples>> {

  private Gauge recoverTimeMetrics;

  @Inject
  public RecoverTimeHandler(@Named("recoverTimeGauge") Gauge recoverTimeMetrics) {
    this.recoverTimeMetrics = recoverTimeMetrics;
  }

  @Override
  public List<MetricFamilySamples> apply(@Nonnull Run successBuild) {
    return Optional.of(successBuild)
        .filter(BuildUtil::isFirstSuccessfulBuildAfterError)
        .map(firstSuccessBuild -> calculateRecoverTime(firstSuccessBuild.getPreviousBuild(), firstSuccessBuild))
        .filter(recoverTime -> recoverTime > 0)
        .map(recoverTime -> setRecoverTimeThenPush(successBuild, recoverTime))
        .orElse(emptyList());
  }

  private List<MetricFamilySamples> setRecoverTimeThenPush(@Nonnull Run successBuild, Long recoverTime) {
    recoverTimeMetrics.clear();
    recoverTimeMetrics.labels(getLabels(successBuild)).set(recoverTime);
    return newArrayList(recoverTimeMetrics.collect());
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
