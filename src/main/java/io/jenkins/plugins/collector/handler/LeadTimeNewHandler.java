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

public class LeadTimeNewHandler implements Function<Run, Long> {


    @Override
    public Long apply(@Nonnull Run successBuild) {
        return Optional.of(successBuild)
                .filter(BuildUtil::isFirstSuccessfulBuildAfterError)
                .map(this::calculateLeadTime)
                .orElse(null);
    }


    private Long calculateLeadTime(Run successBuild) {
        long leadTime = successBuild.getDuration();
        Run previousBuild = successBuild.getPreviousBuild();
        while (!isASuccessAndFinishedMatchedBuild(previousBuild, successBuild)) {
            if (!isAbortBuild(previousBuild)) {
                leadTime = Math.max(leadTime, getBuildEndTime(successBuild) - previousBuild.getStartTimeInMillis());
            }
            previousBuild = previousBuild.getPreviousBuild();
        }
        return leadTime;
    }

    private boolean isASuccessAndFinishedMatchedBuild(Run matchedBuild, Run currentBuild) {
        return matchedBuild == null
                || (!isCompleteOvertime(matchedBuild, currentBuild) && Result.UNSTABLE.isWorseOrEqualTo(matchedBuild.getResult()));
    }

}
