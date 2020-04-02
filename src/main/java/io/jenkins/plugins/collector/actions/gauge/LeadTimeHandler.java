package io.jenkins.plugins.collector.actions.gauge;

import hudson.model.Result;
import hudson.model.Run;
import io.jenkins.plugins.collector.util.CustomizeMetrics;
import io.prometheus.client.Gauge;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static io.jenkins.plugins.collector.util.BuildUtil.getBuildEndTime;
import static io.jenkins.plugins.collector.util.BuildUtil.isCompleteOvertime;
import static io.jenkins.plugins.collector.util.BuildUtil.isFirstSuccessfulBuildAfterError;

public class LeadTimeHandler implements BiConsumer<String[], Run>{
    private CustomizeMetrics customizeMetrics;
    private Gauge leadTimeMetrics;

    public LeadTimeHandler(CustomizeMetrics customizeMetrics, Gauge leadTimeMetrics) {
        this.customizeMetrics = customizeMetrics;
        this.leadTimeMetrics = leadTimeMetrics;
    }

    @Override
    public void accept(String[] labels, Run successBuilds) {
        Optional.of(successBuilds)
                .filter(successBuild -> isFirstSuccessfulBuildAfterError(successBuild.getNextBuild(), successBuild))
                .map(successBuild -> calculateLeadTime(successBuild.getPreviousBuild(), successBuild))
                .filter(leadTime -> leadTime > 0)
                .ifPresent(setLeadTimeThenPush(labels));
    }

    private Consumer<Long> setLeadTimeThenPush(String[] labels) {
        return leadTime -> {
            this.leadTimeMetrics.labels(labels).set(leadTime);
            customizeMetrics.addCollector(this.leadTimeMetrics);
        };
    }

    Long calculateLeadTime(Run matchedBuild, Run currentBuild){
        if (matchedBuild == null ||
                (!isCompleteOvertime(matchedBuild, currentBuild) && Result.UNSTABLE.isWorseOrEqualTo(matchedBuild.getResult()))){
            return currentBuild.getDuration();
        }
        if (Result.ABORTED.equals(matchedBuild.getResult())){
            return calculateLeadTime(matchedBuild.getPreviousBuild(), currentBuild);
        }
        return Math.max(calculateLeadTime(matchedBuild.getPreviousBuild(), currentBuild),
        getBuildEndTime(currentBuild) - matchedBuild.getStartTimeInMillis());
    }
}
