package io.jenkins.plugins.collector.actions.gauge;

import hudson.model.Result;
import hudson.model.Run;
import io.jenkins.plugins.collector.util.CustomizeMetrics;
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

public class RecoverTimeHandler implements BiConsumer<String[], Run>{

    CustomizeMetrics customizeMetrics;

    public RecoverTimeHandler(CustomizeMetrics customizeMetrics) {
        this.customizeMetrics = customizeMetrics;
    }

    private Gauge recoverTimeMetrics = Gauge.build()
            .name(METRICS_NAME_PREFIX + "_failed_build_recovery_time")
            .subsystem(METRICS_SUBSYSTEM).namespace(METRICS_NAMESPACE)
            .labelNames(METRICS_LABEL_NAME_ARRAY)
            .help("Failed Build Recovery Time in milliseconds")
            .create();

    @Override
    public void accept(String[] labels, Run successBuilds) {
        Optional.of(successBuilds)
                .filter(successBuild -> isFirstSuccessfulBuildAfterError(successBuild.getNextBuild(), successBuild))
                .map(successBuild -> calculateRecoverTime(successBuild.getPreviousBuild(), successBuild))
                .filter(recoverTime -> recoverTime > 0)
                .ifPresent(setRecoverTimeThenPush(labels));
    }

    private Consumer<Long> setRecoverTimeThenPush(String[] labels) {
        return recoverTime -> {
            recoverTimeMetrics.labels(labels).set(recoverTime);
            customizeMetrics.addCollector(recoverTimeMetrics);
        };
    }

    Long calculateRecoverTime(Run matchedBuild, Run currentBuild){
        if (matchedBuild == null ||
                (!isCompleteOvertime(matchedBuild, currentBuild)
                        && Result.UNSTABLE.isWorseOrEqualTo(matchedBuild.getResult()))){
            return Long.MIN_VALUE;
        }
        if (Result.ABORTED.equals(matchedBuild.getResult())){
            return calculateRecoverTime(matchedBuild.getPreviousBuild(), currentBuild);
        }
        return Math.max(calculateRecoverTime(matchedBuild.getPreviousBuild(), currentBuild),
        getBuildEndTime(currentBuild) - getBuildEndTime(matchedBuild));
    }
}
