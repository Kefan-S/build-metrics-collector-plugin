package io.jenkins.plugins.collector.service;

import hudson.model.Result;
import hudson.model.Run;
import io.jenkins.plugins.collector.util.BuildUtil;
import java.util.Optional;
import java.util.function.Function;
import javax.annotation.Nonnull;

import static io.jenkins.plugins.collector.util.BuildUtil.getBuildEndTime;
import static io.jenkins.plugins.collector.util.BuildUtil.isAbortBuild;
import static io.jenkins.plugins.collector.util.BuildUtil.isCompleteOvertime;

public class RecoverTimeCalculate implements Function<Run, Long> {


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
