package io.jenkins.plugins.collector.util;

import hudson.model.Cause;
import hudson.model.Job;
import hudson.model.Result;
import hudson.model.Run;
import hudson.triggers.SCMTrigger;
import io.jenkins.plugins.collector.exception.InstanceMissingException;
import java.util.Optional;
import javax.annotation.Nonnull;
import jenkins.model.Jenkins;

import static io.jenkins.plugins.collector.config.Constant.BUILD_NO_RESULT_STATUS;

public class BuildUtil {

  private static final UpstreamJobGetter UPSTREAM_JOB_GETTER = new UpstreamJobGetter();

  public static boolean isFirstSuccessfulBuildAfterError(Run matchedBuild,@Nonnull Run currentBuild) {
    if (!isSuccessfulBuild(currentBuild)) {
      return false;
    }

    if (matchedBuild == null) {
      return true;
    }

    if (isSuccessfulBuild(matchedBuild) && isCompleteOvertime(currentBuild, matchedBuild)) {
      return false;
    }

    return isFirstSuccessfulBuildAfterError(matchedBuild.getNextBuild(), currentBuild);
  }

  public static boolean isCompleteOvertime(Run previousBuild, Run build) {
    return getBuildEndTime(build) - getBuildEndTime(previousBuild) < 0;
  }

  public static long getBuildEndTime(Run build) {
    return build.isBuilding() ? Long.MAX_VALUE :
        (build.getStartTimeInMillis() + build.getDuration());
  }

  public static boolean isAbortBuild(Run build) {
    return !build.isBuilding() && Result.ABORTED.equals(build.getResult());
  }

  public static boolean isSuccessfulBuild(Run build) {
    return !build.isBuilding() && Result.UNSTABLE.isWorseOrEqualTo(build.getResult());
  }

  public static String[] getLabels(Run build) {
    String jobFullName = build.getParent().getFullName();
    String trigger = getTrigger(build);
    String result = Optional.ofNullable(build.getResult()).map(Result::toString)
        .orElse(BUILD_NO_RESULT_STATUS);
    return new String[]{jobFullName, trigger, result};
  }

  static String getTrigger(Run build) {
    Cause.UpstreamCause upstreamCause = (Cause.UpstreamCause) build.getCause(Cause.UpstreamCause.class);
    if (upstreamCause != null) {
      Job job = Optional.ofNullable(Jenkins.getInstanceOrNull())
          .map(r -> UPSTREAM_JOB_GETTER.apply(r, upstreamCause))
          .orElseThrow(InstanceMissingException::new);

      Run upstream = job.getBuildByNumber(upstreamCause.getUpstreamBuild());
      if (upstream != null) {
        return getTrigger(upstream);
      }
    }

    SCMTrigger.SCMTriggerCause scmTriggerCause = (SCMTrigger.SCMTriggerCause) build.getCause(SCMTrigger.SCMTriggerCause.class);
    if (scmTriggerCause != null) {
      return "SCM";
    }

    Cause.UserIdCause userIdCause = (Cause.UserIdCause) build.getCause(Cause.UserIdCause.class);
    if (userIdCause != null) {
      return Optional.ofNullable(userIdCause.getUserId()).orElse("UnKnown User");
    }

    return "UnKnown";
  }
}
