package io.jenkins.plugins.collector.consumer.jenkins;

import hudson.model.Result;
import io.jenkins.plugins.collector.model.BuildInfo;
import io.jenkins.plugins.collector.model.BuildInfoResponse;
import io.jenkins.plugins.collector.model.JenkinsFilterParameter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.apache.commons.lang.StringUtils;

import static java.util.stream.Collectors.toList;

class JenkinsAdapter {

  BuildInfoResponse adapt(List<BuildInfo> buildInfos, JenkinsFilterParameter jenkinsFilterParameter) {
    if (Objects.isNull(buildInfos)) {
      return null;
    }

    List<BuildInfo> validBuilds = buildInfos.stream()
        .filter(buildInfo -> !String.valueOf(Result.ABORTED.ordinal).equals(buildInfo.getResult()))
        .filter(buildInfo -> filterByTime(jenkinsFilterParameter, buildInfo.getStartTime()))
        .filter(buildInfo -> filterByTriggerUser(jenkinsFilterParameter, buildInfo))
        .collect(toList());

    if (validBuilds.isEmpty()) {
      return null;
    }

    BigDecimal failureRate = failureRateCalculate(validBuilds);

    return BuildInfoResponse.builder()
        .failureRate(failureRate)
        .deploymentFrequency(validBuilds.size())
        .buildInfos(validBuilds)
        .build();
  }

  private boolean filterByTriggerUser(JenkinsFilterParameter jenkinsFilterParameter, BuildInfo buildInfo) {
    if (StringUtils.isEmpty(jenkinsFilterParameter.getTriggerBy()) || "All users".equals(jenkinsFilterParameter.getTriggerBy())) {
      return true;
    }
    return buildInfo.getTriggerInfo().getTriggeredBy().equals(jenkinsFilterParameter.getTriggerBy());
  }

  private boolean filterByTime(JenkinsFilterParameter jenkinsFilterParameter, Long buildStartTime) {
    Long beginTime = Optional.ofNullable(jenkinsFilterParameter.getBeginTime()).map(Long::parseLong).orElse(Long.MIN_VALUE);
    Long endTime = Optional.ofNullable(jenkinsFilterParameter.getEndTime()).map(Long::parseLong).orElse(Long.MAX_VALUE);
    return beginTime <= buildStartTime && buildStartTime <= endTime;
  }


  private BigDecimal failureRateCalculate(List<BuildInfo> validBuilds) {
    long failureCount = validBuilds.stream()
        .filter(buildInfo -> !String.valueOf(Result.SUCCESS.ordinal).equals(buildInfo.getResult()))
        .count();
    return new BigDecimal(failureCount).divide(new BigDecimal(validBuilds.size()), 4, RoundingMode.DOWN);
  }

}
