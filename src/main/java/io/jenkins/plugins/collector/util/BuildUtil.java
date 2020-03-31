package io.jenkins.plugins.collector.util;

import hudson.model.Cause;
import hudson.model.Job;
import hudson.model.Result;
import hudson.model.Run;
import hudson.triggers.SCMTrigger;
import jenkins.model.Jenkins;

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

    private static String getTrigger(Run build) {
        Cause.UpstreamCause upstreamCause = (Cause.UpstreamCause) build.getCause(Cause.UpstreamCause.class);
        if (upstreamCause != null) {
            Job job = Jenkins.getInstanceOrNull().getItemByFullName(upstreamCause.getUpstreamProject(), Job.class);
            if (job != null) {
                Run upstream = job.getBuildByNumber(upstreamCause.getUpstreamBuild());
                if (upstream != null) {
                    return getTrigger(upstream);
                }
            }
        }

        SCMTrigger.SCMTriggerCause scmTriggerCause = (SCMTrigger.SCMTriggerCause) build.getCause(SCMTrigger.SCMTriggerCause.class);
        if (scmTriggerCause != null) {
            return "SCM";
        }

        Cause.UserIdCause userIdCause = (Cause.UserIdCause) build.getCause(Cause.UserIdCause.class);
        if(userIdCause != null) {
            return userIdCause.getUserId();
        }

       return null;
    }

    public static String[] getLabels(Run build){
        String jobFullName = build.getParent().getFullName();
        String trigger = getTrigger(build);
        return new String[]{jobFullName, trigger};
    }

}
