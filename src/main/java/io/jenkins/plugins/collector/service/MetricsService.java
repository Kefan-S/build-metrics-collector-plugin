package io.jenkins.plugins.collector.service;

import com.google.inject.Inject;
import hudson.model.Run;
import io.jenkins.plugins.collector.data.BuildProvider;
import io.jenkins.plugins.collector.handler.LeadTimeHandler;
import io.jenkins.plugins.collector.handler.RecoverTimeHandler;
import io.jenkins.plugins.collector.model.BuildInfo;
import io.jenkins.plugins.collector.util.BuildUtil;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class MetricsService {

    LeadTimeHandler leadTimeHandler;
    RecoverTimeHandler recoverTimeHandler;
    BuildProvider buildProvider;

    @Inject
    public MetricsService(LeadTimeHandler leadTimeHandler,
                          RecoverTimeHandler recoverTimeHandler,
                          BuildProvider buildProvider) {
        this.leadTimeHandler = leadTimeHandler;
        this.recoverTimeHandler = recoverTimeHandler;
        this.buildProvider = buildProvider;
    }
    public BuildInfo getMetrics(Run run) {
        return Optional.ofNullable(run).map(build -> BuildInfo.builder().duration(build.getDuration())
            .leadTime(calculateLeadTime(build))
            .recoverTime(calculateRecoverTime(build))
            .startTime(build.getStartTimeInMillis())
            .jenkinsJob(BuildUtil.getJobName(build))
            .result(BuildUtil.getResultValue(build))
            .triggeredBy(BuildUtil.getTrigger(build))
            .build()).orElse(BuildInfo.builder().build());
    }

    public List<BuildInfo> getAllMetrics() {
        return buildProvider.getNeedToHandleBuilds().stream().map(this::getMetrics).collect(Collectors.toList());
    }

    public Long calculateLeadTime(Run build) {
        return leadTimeHandler.apply(build);
    }

    public Long calculateRecoverTime(Run build) {
        return  recoverTimeHandler.apply(build);
    }

}
