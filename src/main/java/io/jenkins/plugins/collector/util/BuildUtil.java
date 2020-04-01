package io.jenkins.plugins.collector.util;

import hudson.model.Cause;
import hudson.model.Job;
import hudson.model.Result;
import hudson.model.Run;
import hudson.triggers.SCMTrigger;
import io.jenkins.plugins.collector.exceptions.NoJenkinsException;
import jenkins.model.Jenkins;

import java.util.Optional;

public class BuildUtil {
    public static boolean isFirstSuccessfulBuildAfterError(Run matchedBuild, Run currentBuild) {
        if (matchedBuild == null) {
            return true;
        }
        if (Result.UNSTABLE.isWorseOrEqualTo(currentBuild.getResult()) && isCompleteOvertime(currentBuild, matchedBuild)) {
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

    private static String getTrigger(Run build) {
        Cause.UpstreamCause upstreamCause = (Cause.UpstreamCause) build.getCause(Cause.UpstreamCause.class);
        if (upstreamCause != null) {
            Job job = Optional.ofNullable(Jenkins.getInstanceOrNull())
                    .map(r -> r.getItemByFullName(upstreamCause.getUpstreamProject(), Job.class))
                    .orElseThrow(NoJenkinsException::new);

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
            return Optional.ofNullable(userIdCause.getUserId()).orElse("Unknown User");
        }

       return "UnKnown";
    }

    public static String[] getLabels(Run build){
        String jobFullName = build.getParent().getFullName();
        String trigger = getTrigger(build);
        return new String[]{jobFullName, trigger};
    }

}
