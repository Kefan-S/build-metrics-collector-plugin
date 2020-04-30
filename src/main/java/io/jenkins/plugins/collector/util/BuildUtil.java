package io.jenkins.plugins.collector.util;

import hudson.model.Cause;
import hudson.model.FreeStyleBuild;
import hudson.model.Job;
import hudson.model.Result;
import hudson.model.Run;
import hudson.plugins.git.GitChangeSet;
import hudson.scm.ChangeLogSet;
import hudson.scm.ChangeLogSet.Entry;
import hudson.triggers.SCMTrigger;
import io.jenkins.plugins.collector.exception.InstanceMissingException;
import io.jenkins.plugins.collector.model.SCMChangeInfo;
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

  public static boolean isCompleteOvertime(@Nonnull Run previousBuild,@Nonnull  Run build) {
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
    String trigger = getTrigger(build).getTriggerBy();
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
      String jenkinsUserName = Optional.ofNullable(userIdCause.getUserId()).orElse("UnKnown User");
      TriggerEnum triggerEnum = TriggerEnum.MANUAL_TRIGGER;
      triggerEnum.setTriggerBy(jenkinsUserName);
      return triggerEnum;
    }

    return TriggerEnum.UNKNOWN;
  }

  public static TriggerInfo getTriggerInfo(Run build) {
    return TriggerInfo.builder()
        .triggerType(getTrigger(build))
        .scmChangeInfoList(getSCMChangeInfo(build))
        .build();
  }

  private static List<SCMChangeInfo> getSCMChangeInfo(Run build) {
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

  private static List<SCMChangeInfo> getTriggerInfoForFreeStyleBuild(FreeStyleBuild freeStyleBuild) {

    return getTriggerInfoByChangeLogSet(freeStyleBuild.getChangeSets());
  }

  private static List<SCMChangeInfo> getTriggerInfoForWorkflowRun(WorkflowRun workflowRun) {
    return getTriggerInfoByChangeLogSet(workflowRun.getChangeSets());
  }

  private static List<SCMChangeInfo> getTriggerInfoByChangeLogSet(List<ChangeLogSet<? extends Entry>> changeSets) {
    if (CollectionUtils.isEmpty(changeSets)) {
      return Collections.emptyList();
    }
    List<SCMChangeInfo> changeInfos = new ArrayList<>();
    Object[] items =  changeSets.get(0).getItems();
    for (Object changeSet : items) {
      if (changeSet instanceof GitChangeSet) {
        changeInfos.add(buildSCMChangeInfoFromGitChangeSet((GitChangeSet) changeSet));
      }
    }
    return changeInfos;
  }

  private static SCMChangeInfo buildSCMChangeInfoFromGitChangeSet(GitChangeSet changeSet) {
    return SCMChangeInfo.builder()
            .userId(changeSet.getAuthorName())
            .commitHash(changeSet.getCommitId())
            .commitTimeStamp(changeSet.getTimestamp())
            .commitMessage(changeSet.getComment())
            .build();
  }
}
