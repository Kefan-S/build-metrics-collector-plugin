package io.jenkins.plugins.collector.handler;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import hudson.model.Result;
import hudson.model.Run;
import io.jenkins.plugins.collector.util.BuildUtil;
import io.prometheus.client.Collector.MetricFamilySamples;
import io.prometheus.client.Gauge;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import javax.annotation.Nonnull;

import static com.google.common.collect.Lists.newArrayList;
import static io.jenkins.plugins.collector.util.BuildUtil.getBuildEndTime;
import static io.jenkins.plugins.collector.util.BuildUtil.getLabels;
import static io.jenkins.plugins.collector.util.BuildUtil.isAbortBuild;
import static io.jenkins.plugins.collector.util.BuildUtil.isCompleteOvertime;
import static java.util.Collections.emptyList;

public class LeadTimeHandler extends AbstractHandler implements Function<Run, List<MetricFamilySamples>> {

  private Gauge leadTimeMetrics;

  @Inject
  public LeadTimeHandler(@Named("leadTimeGauge") Gauge leadTimeMetrics) {
    this.leadTimeMetrics = leadTimeMetrics;
  }

  @Override
  public List<MetricFamilySamples> apply(@Nonnull Run successBuild) {
    return Optional.of(successBuild)
        .filter(BuildUtil::isFirstSuccessfulBuildAfterError)
        .map(firstSuccessBuild -> calculateLeadTime(firstSuccessBuild.getPreviousBuild(), firstSuccessBuild))
        .map(leadTime -> setLeadTimeThenPush(successBuild, leadTime))
        .orElse(emptyList());
  }

  private List<MetricFamilySamples> setLeadTimeThenPush(@Nonnull Run successBuild, Long leadTime) {
    processMetrics(newArrayList(leadTimeMetrics), successBuild, leadTime);
    return newArrayList(leadTimeMetrics.collect());
  }

  private Long calculateLeadTime(Run matchedBuild, Run successBuild) {
    long leadTime = successBuild.getDuration();
    while (!isASuccessAndFinishedMatchedBuild(matchedBuild, successBuild)) {
      if (!isAbortBuild(matchedBuild)) {
        leadTime = Math.max(leadTime, getBuildEndTime(successBuild) - matchedBuild.getStartTimeInMillis());
      }
      matchedBuild = matchedBuild.getPreviousBuild();
    }
    return leadTime;
  }

  private boolean isASuccessAndFinishedMatchedBuild(Run matchedBuild, Run currentBuild) {
    return matchedBuild == null
        || (!isCompleteOvertime(matchedBuild, currentBuild) && Result.UNSTABLE.isWorseOrEqualTo(matchedBuild.getResult()));
  }

  @Override
  void setMetricValue(Run build, Long metricValue) {
    this.leadTimeMetrics.labels(getLabels(build)).set(metricValue);
  }
}
