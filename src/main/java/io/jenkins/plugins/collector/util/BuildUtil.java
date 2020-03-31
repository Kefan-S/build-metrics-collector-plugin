package io.jenkins.plugins.collector.util;

import hudson.model.Result;
import hudson.model.Run;

public class BuildUtil {
    public static boolean isFirstSuccessfulBuildAfterError(Run matchedBuild, Run currentBuild) {
        if (matchedBuild == null) {
            return true;
        }
        if (Result.UNSTABLE.isWorseOrEqualTo(currentBuild.getResult()) && isCompleteOvertime(currentBuild, matchedBuild)){
            return false;
        }
        return isFirstSuccessfulBuildAfterError(matchedBuild.getNextBuild(), currentBuild);
    }

    public static boolean isCompleteOvertime(Run previousBuild, Run build) {
        return getBuildEndTime(build) - getBuildEndTime(previousBuild) < 0;
    }

    public static long getBuildEndTime(Run build) {
        return build.getStartTimeInMillis() + build.getDuration();
    }

    public static boolean isAbortBuild(Run build) {
        return !build.isBuilding() && Result.ABORTED.equals(build.getResult());
    }

    public static boolean isSuccessfulBuild(Run build) {
        return !build.isBuilding() && Result.UNSTABLE.isWorseOrEqualTo(build.getResult());
    }

}
