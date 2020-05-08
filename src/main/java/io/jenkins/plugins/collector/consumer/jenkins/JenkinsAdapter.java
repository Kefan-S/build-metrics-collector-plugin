package io.jenkins.plugins.collector.consumer.jenkins;

import hudson.model.Result;
import io.jenkins.plugins.collector.model.BuildInfo;
import io.jenkins.plugins.collector.model.BuildInfoResponse;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import static java.util.stream.Collectors.toList;

class JenkinsAdapter {

  BuildInfoResponse adapt(List<BuildInfo> buildInfos) {

    List<BuildInfo> validBuilds = buildInfos.stream()
        .filter(buildInfo -> !String.valueOf(Result.ABORTED.ordinal).equals(buildInfo.getResult()))
        .collect(toList());
    long failureCount = validBuilds.stream()
        .filter(buildInfo -> !String.valueOf(Result.SUCCESS.ordinal).equals(buildInfo.getResult()))
        .count();
    BigDecimal failureRate = new BigDecimal(failureCount).divide(new BigDecimal(validBuilds.size()), 3, RoundingMode.HALF_UP);

    List<Long> startTime = validBuilds.stream().map(BuildInfo::getStartTime).collect(toList());
    List<Long> duration = validBuilds.stream().map(BuildInfo::getDuration).collect(toList());
    List<Long> leadTime = validBuilds.stream().map(BuildInfo::getLeadTime).collect(toList());
    List<Long> recoverTime = validBuilds.stream().map(BuildInfo::getRecoverTime).collect(toList());

    return BuildInfoResponse.builder()
        .failureRate(failureRate)
        .deploymentFrequency(validBuilds.size())
        .duration(duration)
        .leadTime(leadTime)
        .recoverTime(recoverTime)
        .startTime(startTime).build();
  }

}
