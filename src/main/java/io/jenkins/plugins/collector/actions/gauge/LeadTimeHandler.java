package io.jenkins.plugins.collector.actions.gauge;

import hudson.model.Result;
import hudson.model.Run;
import io.prometheus.client.Gauge;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static io.jenkins.plugins.collector.config.Constant.METRICS_LABEL_NAME_ARRAY;
import static io.jenkins.plugins.collector.config.Constant.METRICS_NAMESPACE;
import static io.jenkins.plugins.collector.config.Constant.METRICS_NAME_PREFIX;
import static io.jenkins.plugins.collector.config.Constant.METRICS_SUBSYSTEM;
import static io.jenkins.plugins.collector.util.BuildUtil.getBuildEndTime;
import static io.jenkins.plugins.collector.util.BuildUtil.isCompleteOvertime;
import static io.jenkins.plugins.collector.util.BuildUtil.isFirstSuccessfulBuildAfterError;
import static io.jenkins.plugins.collector.util.CustomizeMetrics.addCollector;

public class LeadTimeHandler implements BiConsumer<String, Run>{
    public Gauge LEAD_TIME_IN_LAST_STATISTICAL_PERIOD = Gauge.build()
            .name(METRICS_NAME_PREFIX + "_merge_lead_time")
            .subsystem(METRICS_SUBSYSTEM).namespace(METRICS_NAMESPACE)
            .labelNames(METRICS_LABEL_NAME_ARRAY)
            .help("Code Merge Lead Time in milliseconds")
            .create();

    @Override
    public void accept(String label, Run successBuilds) {
        Optional.of(successBuilds)
                .filter(successBuild -> isFirstSuccessfulBuildAfterError(successBuild.getNextBuild(), successBuild))
                .map(successBuild -> calculateLeadTime(successBuild.getPreviousBuild(), successBuild))
                .filter(leadTime -> leadTime > 0)
                .ifPresent(setLeadTimeThenPush(label));
    }

    private Consumer<Long> setLeadTimeThenPush(String label) {
        return leadTime -> {
            LEAD_TIME_IN_LAST_STATISTICAL_PERIOD.labels(label).set(leadTime);
            addCollector(LEAD_TIME_IN_LAST_STATISTICAL_PERIOD);
        };
    }

    Long calculateLeadTime(Run matchedBuild, Run currentBuild){
        if (matchedBuild == null ||
                (!isCompleteOvertime(matchedBuild, currentBuild) && Result.UNSTABLE.isWorseOrEqualTo(matchedBuild.getResult()))){
            return currentBuild.getDuration();
        }
        if (Result.ABORTED.equals(matchedBuild.getResult()) && matchedBuild.isBuilding()){
            return calculateLeadTime(matchedBuild.getPreviousBuild(), currentBuild);
        }
        return Math.max(calculateLeadTime(matchedBuild.getPreviousBuild(), currentBuild),
        getBuildEndTime(currentBuild) - matchedBuild.getStartTimeInMillis());
    }
}
