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

public class RecoveryTimeCalculate implements Function<Run, Long> {


  @Override
  public Long apply(@Nonnull Run successBuild) {
    return Optional.of(successBuild)
        .filter(BuildUtil::isFirstSuccessfulBuildAfterError)
        .map(this::calculateRecoveryTime)
        .filter(recoveryTime -> recoveryTime > 0)
        .orElse(null);
  }


  Long calculateRecoveryTime(Run currentBuild) {
    long recoveryTime = Long.MIN_VALUE;
    Run previousBuild = currentBuild.getPreviousBuild();
    while (!isASuccessAndFinishedMatchedBuild(previousBuild, currentBuild)) {
      if (!isAbortBuild(previousBuild)) {
        recoveryTime = Math.max(recoveryTime, getBuildEndTime(currentBuild) - getBuildEndTime(previousBuild));
      }
      previousBuild = previousBuild.getPreviousBuild();
    }
    return recoveryTime;
  }

  private boolean isASuccessAndFinishedMatchedBuild(Run matchedBuild, Run currentBuild) {
    return matchedBuild == null
        || (!isCompleteOvertime(matchedBuild, currentBuild) && Result.UNSTABLE.isWorseOrEqualTo(matchedBuild.getResult()));
  }

}
