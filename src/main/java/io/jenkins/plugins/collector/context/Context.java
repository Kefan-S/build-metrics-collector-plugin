package io.jenkins.plugins.collector.context;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import hudson.Extension;
import hudson.model.Run;
import io.jenkins.plugins.collector.actions.gauge.BuildInfoHandler;
import io.jenkins.plugins.collector.actions.gauge.LeadTimeHandler;
import io.jenkins.plugins.collector.actions.gauge.RecoverTimeHandler;
import io.jenkins.plugins.collector.data.CustomizeMetrics;
import io.jenkins.plugins.collector.service.DefaultPrometheusMetrics;
import io.jenkins.plugins.collector.service.PrometheusMetrics;
import io.prometheus.client.Gauge;
import java.util.function.Consumer;

import static io.jenkins.plugins.collector.config.Constant.METRICS_LABEL_NAME_ARRAY;
import static io.jenkins.plugins.collector.config.Constant.METRICS_NAMESPACE;
import static io.jenkins.plugins.collector.config.Constant.METRICS_NAME_PREFIX;
import static io.jenkins.plugins.collector.config.Constant.METRICS_SUBSYSTEM;

@Extension
public class Context extends AbstractModule {

  @Override
  public void configure() {
    bind(PrometheusMetrics.class).to(DefaultPrometheusMetrics.class).in(Singleton.class);
    bind(CustomizeMetrics.class).in(Singleton.class);
  }

  @Provides
  @Singleton
  @Named("successBuildHandler")
  Consumer<Run> successBuildHandler(CustomizeMetrics customizeMetrics) {
      Gauge leadTimeMetrics = Gauge.build()
          .name(METRICS_NAME_PREFIX + "_merge_lead_time")
          .subsystem(METRICS_SUBSYSTEM).namespace(METRICS_NAMESPACE)
          .labelNames(METRICS_LABEL_NAME_ARRAY.toArray(new String[0]))
          .help("Code Merge Lead Time in milliseconds")
          .create();

    Gauge recoverTimeMetrics = Gauge.build()
        .name(METRICS_NAME_PREFIX + "_failed_build_recovery_time")
        .subsystem(METRICS_SUBSYSTEM).namespace(METRICS_NAMESPACE)
        .labelNames(METRICS_LABEL_NAME_ARRAY.toArray(new String[0]))
        .help("Failed Build Recovery Time in milliseconds")
        .create();

    customizeMetrics.addCollector(leadTimeMetrics);
    customizeMetrics.addCollector(recoverTimeMetrics);

    return new LeadTimeHandler(leadTimeMetrics)
        .andThen(new RecoverTimeHandler(recoverTimeMetrics));
  }

  @Provides
  @Singleton
  @Named("buildInfoHandler")
  Consumer<Run> buildInfoHandler(CustomizeMetrics customizeMetrics) {
    Gauge buildDurationMetrics = Gauge.build()
        .name(METRICS_NAME_PREFIX + "_last_build_duration_in_milliseconds")
        .subsystem(METRICS_SUBSYSTEM).namespace(METRICS_NAMESPACE)
        .labelNames(METRICS_LABEL_NAME_ARRAY.toArray(new String[0]))
        .help("One build duration in milliseconds")
        .create();

    Gauge buildStartTimeMetrics = Gauge.build()
        .name(METRICS_NAME_PREFIX + "_last_build_start_timestamp")
        .subsystem(METRICS_SUBSYSTEM).namespace(METRICS_NAMESPACE)
        .labelNames(METRICS_LABEL_NAME_ARRAY.toArray(new String[0]))
        .help("One build start timestamp")
        .create();

    customizeMetrics.addCollector(buildDurationMetrics);
    customizeMetrics.addCollector(buildStartTimeMetrics);

    return new BuildInfoHandler(buildDurationMetrics, buildStartTimeMetrics);
  }

  @Provides
  @Singleton
  @Named("buildHandler")
  Consumer<Run> buildHandler(@Named("successBuildHandler")Consumer<Run> successBuildHandler,
                             @Named("buildInfoHandler")Consumer<Run> buildInfoHandler){
    return successBuildHandler.andThen(buildInfoHandler);
  }
}
