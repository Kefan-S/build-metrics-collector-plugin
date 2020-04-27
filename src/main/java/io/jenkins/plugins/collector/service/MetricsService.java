package io.jenkins.plugins.collector.service;

import com.google.inject.Inject;
import hudson.model.Run;
import io.jenkins.plugins.collector.data.BuildProvider;
import io.jenkins.plugins.collector.handler.LeadTimeNewHandler;
import io.jenkins.plugins.collector.handler.RecoverTimeNewHandler;
import io.jenkins.plugins.collector.model.Metrics;
import io.jenkins.plugins.collector.util.BuildUtil;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class MetricsService {

    LeadTimeNewHandler leadTimeNewHandler;
    RecoverTimeNewHandler recoverTimeNewHandler;
    BuildProvider buildProvider;

    @Inject
    public MetricsService(LeadTimeNewHandler leadTimeNewHandler,
                          RecoverTimeNewHandler recoverTimeNewHandler,
                          BuildProvider buildProvider) {
        this.leadTimeNewHandler = leadTimeNewHandler;
        this.recoverTimeNewHandler = recoverTimeNewHandler;
        this.buildProvider = buildProvider;
    }
    public Metrics getMetrics(Run run) {
        return Optional.ofNullable(run).map(build -> Metrics.builder().duration(build.getDuration())
            .leadTime(calculateLeadTime(build))
            .recoverTime(calculateRecoverTime(build))
            .startTime(build.getStartTimeInMillis())
            .jenkinsJob(BuildUtil.getJobName(build))
            .result(BuildUtil.getResultValue(build))
            .triggeredBy(BuildUtil.getTrigger(build))
            .build()).orElse(Metrics.builder().build());
    }

    public List<Metrics> getAllMetrics() {
        return buildProvider.getNeedToHandleBuilds().stream().map(this::getMetrics).collect(Collectors.toList());
    }

    public Long calculateLeadTime(Run build) {
        return leadTimeNewHandler.apply(build);
    }

    public Long calculateRecoverTime(Run build) {
        return  recoverTimeNewHandler.apply(build);
    }

}
