package io.jenkins.plugins.collector.service;

import com.google.inject.Inject;
import hudson.model.Run;
import io.jenkins.plugins.collector.data.BuildProvider;
import io.jenkins.plugins.collector.model.BuildInfo;
import io.jenkins.plugins.collector.util.BuildUtil;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class BuildInfoService {

  LeadTimeCalculate leadTimeCalculate;
  RecoverTimeCalculate recoverTimeCalculate;
  BuildProvider buildProvider;

  @Inject
  public BuildInfoService(LeadTimeCalculate leadTimeCalculate,
                          RecoverTimeCalculate recoverTimeCalculate,
                          BuildProvider buildProvider) {
    this.leadTimeCalculate = leadTimeCalculate;
    this.recoverTimeCalculate = recoverTimeCalculate;
    this.buildProvider = buildProvider;
  }

  public BuildInfo getBuildInfo(Run run) {
    return Optional.ofNullable(run).map(build -> BuildInfo.builder().duration(build.getDuration())
        .leadTime(calculateLeadTime(build))
        .recoverTime(calculateRecoverTime(build))
        .startTime(build.getStartTimeInMillis())
        .id(build.getId())
        .jenkinsJob(BuildUtil.getJobName(build))
        .result(BuildUtil.getResultValue(build))
        .triggerInfo(BuildUtil.getTriggerInfo(build))
        .build()).orElse(BuildInfo.builder().build());
  }

  public List<BuildInfo> getAllBuildInfo() {
    List<Run> needToHandleBuilds = buildProvider.getNeedToHandleBuilds();
    List<BuildInfo> buildInfos = needToHandleBuilds.stream().map(this::getBuildInfo).collect(Collectors.toList());
    needToHandleBuilds.forEach(build -> buildProvider.remove(build));
    return buildInfos;
  }

  public Long calculateLeadTime(Run build) {
    return leadTimeCalculate.apply(build);
  }

  public Long calculateRecoverTime(Run build) {
    return recoverTimeCalculate.apply(build);
  }

}
