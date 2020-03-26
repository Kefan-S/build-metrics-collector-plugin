package io.jenkins.plugins.collector.actions;

import hudson.model.Result;
import hudson.model.Run;
import io.prometheus.client.Collector;
import io.prometheus.client.Counter;
import io.reactivex.rxjava3.functions.Consumer;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.jenkins.plugins.collector.config.Constant.FULLNAME;
import static io.jenkins.plugins.collector.config.Constant.LABEL_NAME_ARRAY;
import static io.jenkins.plugins.collector.config.Constant.NAMESPACE;
import static io.jenkins.plugins.collector.config.Constant.SUBSYSTEM;


public class BuildStatusCounter implements Consumer<Run> {

    private Counter jobSuccessCount;
    private Counter jobTotalCount;
    private Counter jobFailedCount;
    private static BuildStatusCounter buildStatusCounter;

    private BuildStatusCounter(Counter totalCount,
                              Counter successCount,
                              Counter failedCount) {
        this.jobSuccessCount = successCount;
        this.jobTotalCount = totalCount;
        this.jobFailedCount = failedCount;
    }

    public static BuildStatusCounter getInstance() {
        if (Objects.isNull(buildStatusCounter)) {
            Counter jobSuccessCount = Counter.build()
                    .name(FULLNAME + "_success_build_total_count")
                    .subsystem(SUBSYSTEM).namespace(NAMESPACE)
                    .labelNames(LABEL_NAME_ARRAY)
                    .help("Successful build count")
                    .create();

            Counter jobTotalCount = Counter.build()
                    .name(FULLNAME + "_build_total_count")
                    .subsystem(SUBSYSTEM).namespace(NAMESPACE)
                    .labelNames(LABEL_NAME_ARRAY)
                    .help("All build count")
                    .create();

            Counter jobFailedCount = Counter.build()
                    .name(FULLNAME + "_failed_build_total_count")
                    .subsystem(SUBSYSTEM).namespace(NAMESPACE)
                    .labelNames(LABEL_NAME_ARRAY)
                    .help("Failed build count")
                    .create();

            buildStatusCounter = new BuildStatusCounter(jobTotalCount, jobSuccessCount, jobFailedCount);
        }

        return buildStatusCounter;
    }

    public List<Collector.MetricFamilySamples> getMetricsList() {
        return Stream.of(jobSuccessCount, jobTotalCount, jobFailedCount)
                .map(Counter::collect)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    @Override
    public void accept(Run run) {
        if(Result.ABORTED.equals(run.getResult())) return;

        if (Result.SUCCESS.equals(run.getResult())) {
            jobSuccessCount.labels(run.getParent().getFullName()).inc();
        }

        if (Result.FAILURE.equals(run.getResult())) {
            jobFailedCount.labels(run.getParent().getFullName()).inc();
        }

        jobTotalCount.labels(run.getParent().getFullName()).inc();
    }
}
