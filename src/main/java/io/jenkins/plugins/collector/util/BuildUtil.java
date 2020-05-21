package io.jenkins.plugins.collector.util;

import hudson.model.Cause;
import hudson.model.Cause.UpstreamCause;
import hudson.model.Cause.UserIdCause;
import hudson.model.FreeStyleBuild;
import hudson.model.Job;
import hudson.model.Result;
import hudson.model.Run;
import hudson.plugins.git.GitChangeSet;
import hudson.scm.ChangeLogSet;
import hudson.scm.ChangeLogSet.Entry;
import hudson.triggers.SCMTrigger;
import hudson.triggers.SCMTrigger.SCMTriggerCause;
import io.jenkins.plugins.collector.exception.InstanceMissingException;
import io.jenkins.plugins.collector.model.ScmChangeInfo;
import io.jenkins.plugins.collector.model.TriggerEnum;
import io.jenkins.plugins.collector.model.TriggerInfo;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nonnull;
import jenkins.model.Jenkins;
import org.apache.commons.collections.CollectionUtils;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;

import static io.jenkins.plugins.collector.config.Constant.BUILD_NO_RESULT_STATUS_VALUE;

public class BuildUtil {

  private static final UpstreamJobGetter UPSTREAM_JOB_GETTER = new UpstreamJobGetter();

  public static boolean isFirstSuccessfulBuildAfterError(@Nonnull Run currentBuild) {
    if (!isSuccessfulBuild(currentBuild)) {
      return false;
    }

    Run matchedBuild = currentBuild.getNextBuild();

    while (matchedBuild != null) {
      if (isSuccessfulBuild(matchedBuild) && isCompleteOvertime(currentBuild, matchedBuild)) {
        return false;
      }
      matchedBuild = matchedBuild.getNextBuild();
    }

    return true;
  }

  public static boolean isCompleteOvertime(@Nonnull Run previousBuild, @Nonnull Run build) {
    return getBuildEndTime(build) - getBuildEndTime(previousBuild) < 0;
  }

  public static long getBuildEndTime(@Nonnull Run build) {
    return build.isBuilding() ? Long.MAX_VALUE :
        (build.getStartTimeInMillis() + build.getDuration());
  }

  public static boolean isAbortBuild(@Nonnull Run build) {
    return !build.isBuilding() && Result.ABORTED.equals(build.getResult());
  }

  public static boolean isSuccessfulBuild(@Nonnull Run build) {
    return !build.isBuilding() && Result.UNSTABLE.isWorseOrEqualTo(build.getResult());
  }

  public static String[] getLabels(@Nonnull Run build) {
    String jobFullName = getJobName(build);
    String trigger = "SCM";
    String resultValue = getResultValue(build);
    return new String[]{jobFullName, trigger, resultValue};
  }

  public static String getJobName(@Nonnull Run build) {
    return build.getParent().getFullName();
  }

  public static String getResultValue(@Nonnull Run build) {
    return Optional.ofNullable(build.getResult()).map(result -> String.valueOf(result.ordinal))
        .orElse(BUILD_NO_RESULT_STATUS_VALUE);
  }


  public static TriggerEnum getTrigger(@Nonnull Run build) {
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
      return TriggerEnum.SCM_TRIGGER;
    }

    Cause.UserIdCause userIdCause = (Cause.UserIdCause) build.getCause(Cause.UserIdCause.class);
    if (userIdCause != null) {
      TriggerEnum triggerEnum = TriggerEnum.MANUAL_TRIGGER;
      return triggerEnum;
    }

    return TriggerEnum.UNKNOWN;
  }

  public static Cause getOriginalCause(@Nonnull Run build) {
    Cause.UpstreamCause upstreamCause = (Cause.UpstreamCause) build.getCause(Cause.UpstreamCause.class);
    if (upstreamCause != null) {
      Run upstream = getUpstreamRunByUpstreamCause(upstreamCause);
      if (upstream != null) {
        return getOriginalCause(upstream);
      }
    }

    return (Cause) build.getCauses().get(0);
  }

  private static Run getUpstreamRunByUpstreamCause(UpstreamCause upstreamCause) {
    Job job = Optional.ofNullable(Jenkins.getInstanceOrNull())
        .map(r -> UPSTREAM_JOB_GETTER.apply(r, upstreamCause))
        .orElseThrow(InstanceMissingException::new);

    return job.getBuildByNumber(upstreamCause.getUpstreamBuild());
  }

  public static TriggerInfo getTriggerInfo(Run build) {
    Cause originalCause = getOriginalCause(build);

    TriggerEnum triggerType = TriggerEnum.UNKNOWN;
    String triggeredBy = "UnKnown";
    final List<ScmChangeInfo> scmChangeInfos = getScmChangeInfo(build);

    if (originalCause instanceof SCMTriggerCause) {
      triggerType = TriggerEnum.SCM_TRIGGER;
      triggeredBy = getLastCommitUserId(scmChangeInfos);
    }

    if (originalCause instanceof UserIdCause) {
      triggerType = TriggerEnum.MANUAL_TRIGGER;
      UserIdCause userIdCause = (UserIdCause) originalCause;
      triggeredBy = Optional.ofNullable(userIdCause.getUserId()).orElse("UnKnown User");
    }

    String lastCommitHash = scmChangeInfos.stream()
        .reduce((first, second) -> second)
        .map(ScmChangeInfo::getCommitHash).orElse("");

    return TriggerInfo.builder()
        .triggerType(triggerType)
        .scmChangeInfoList(scmChangeInfos)
        .triggeredBy(triggeredBy)
        .lastCommitHash(lastCommitHash)
        .build();
  }

  private static String getLastCommitUserId(List<ScmChangeInfo> scmChangeInfos) {
    return scmChangeInfos.stream()
        .reduce((first, second) -> second)
        .map(ScmChangeInfo::getUserId)
        .orElse("SCM");
  }

  static List<ScmChangeInfo> getScmChangeInfo(Run build) {
    if (build instanceof WorkflowRun) {
      WorkflowRun workflowRun = (WorkflowRun) build;
      return getTriggerInfoForWorkflowRun(workflowRun);
    }
    if (build instanceof FreeStyleBuild) {
      FreeStyleBuild freeStyleBuild = (FreeStyleBuild) build;
      return getTriggerInfoForFreeStyleBuild(freeStyleBuild);
    }
    return null;
  }

  private static List<ScmChangeInfo> getTriggerInfoForFreeStyleBuild(FreeStyleBuild freeStyleBuild) {

    return getTriggerInfoByChangeLogSet(freeStyleBuild.getChangeSets());
  }

  private static List<ScmChangeInfo> getTriggerInfoForWorkflowRun(WorkflowRun workflowRun) {
    return getTriggerInfoByChangeLogSet(workflowRun.getChangeSets());
  }

  private static List<ScmChangeInfo> getTriggerInfoByChangeLogSet(List<ChangeLogSet<? extends Entry>> changeSets) {
    if (CollectionUtils.isEmpty(changeSets)) {
      return Collections.emptyList();
    }
    List<ScmChangeInfo> changeInfos = new ArrayList<>();
    Object[] items = changeSets.get(0).getItems();
    for (Object changeSet : items) {
      if (changeSet instanceof GitChangeSet) {
        changeInfos.add(buildScmChangeInfoFromGitChangeSet((GitChangeSet) changeSet));
      }
    }
    return changeInfos;
  }

  private static ScmChangeInfo buildScmChangeInfoFromGitChangeSet(GitChangeSet changeSet) {
    return ScmChangeInfo.builder()
        .userId(changeSet.getAuthorName())
        .commitHash(changeSet.getCommitId())
        .commitTimeStamp(changeSet.getTimestamp())
        .commitMessage(changeSet.getComment())
        .build();
  }
}
