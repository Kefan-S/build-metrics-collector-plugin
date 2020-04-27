package io.jenkins.plugins.collector.handler;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import hudson.model.Result;
import hudson.model.Run;
import io.jenkins.plugins.collector.util.BuildUtil;
import io.prometheus.client.Collector.MetricFamilySamples;
import io.prometheus.client.Gauge;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static com.google.common.collect.Lists.newArrayList;
import static io.jenkins.plugins.collector.util.BuildUtil.*;
import static java.util.Collections.emptyList;

public class RecoverTimeNewHandler implements Function<Run, Long> {


  @Override
  public Long apply(@Nonnull Run successBuild) {
    return Optional.of(successBuild)
        .filter(BuildUtil::isFirstSuccessfulBuildAfterError)
        .map(this::calculateRecoverTime)
        .filter(recoverTime -> recoverTime > 0)
        .orElse(null);
  }


  Long calculateRecoverTime(Run currentBuild) {
    long recoverTime = Long.MIN_VALUE;
    Run previousBuild = currentBuild.getPreviousBuild();
    while (!isASuccessAndFinishedMatchedBuild(previousBuild, currentBuild)) {
      if (!isAbortBuild(previousBuild)) {
        recoverTime = Math.max(recoverTime, getBuildEndTime(currentBuild) - getBuildEndTime(previousBuild));
      }
      previousBuild = previousBuild.getPreviousBuild();
    }
    return recoverTime;
  }

  private boolean isASuccessAndFinishedMatchedBuild(Run matchedBuild, Run currentBuild) {
    return matchedBuild == null
        || (!isCompleteOvertime(matchedBuild, currentBuild) && Result.UNSTABLE.isWorseOrEqualTo(matchedBuild.getResult()));
  }

}
