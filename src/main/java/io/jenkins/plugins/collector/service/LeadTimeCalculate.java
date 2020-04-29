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

public class LeadTimeCalculate implements Function<Run, Long> {

  @Override
  public Long apply(@Nonnull Run successBuild) {
    return Optional.of(successBuild)
        .filter(BuildUtil::isFirstSuccessfulBuildAfterError)
        .map(this::calculateLeadTime)
        .orElse(null);
  }


  private Long calculateLeadTime(Run successBuild) {
    long leadTime = successBuild.getDuration();
    Run previousBuild = successBuild.getPreviousBuild();
    while (!isASuccessAndFinishedMatchedBuild(previousBuild, successBuild)) {
      if (!isAbortBuild(previousBuild)) {
        leadTime = Math.max(leadTime, getBuildEndTime(successBuild) - previousBuild.getStartTimeInMillis());
      }
      previousBuild = previousBuild.getPreviousBuild();
    }
    return leadTime;
  }

  private boolean isASuccessAndFinishedMatchedBuild(Run matchedBuild, Run currentBuild) {
    return matchedBuild == null
        || (!isCompleteOvertime(matchedBuild, currentBuild) && Result.UNSTABLE.isWorseOrEqualTo(matchedBuild.getResult()));
  }

}
