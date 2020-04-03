package io.jenkins.plugins.collector.context;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.google.inject.name.Names;
import hudson.Extension;
import hudson.model.Run;
import io.jenkins.plugins.collector.actions.BuildMetricsCalculator;
import io.jenkins.plugins.collector.actions.gauge.BuildInfoHandler;
import io.jenkins.plugins.collector.actions.gauge.LeadTimeHandler;
import io.jenkins.plugins.collector.actions.gauge.RecoverTimeHandler;
import io.jenkins.plugins.collector.service.DefaultPrometheusMetrics;
import io.jenkins.plugins.collector.service.PrometheusMetrics;
import io.jenkins.plugins.collector.util.CustomizeMetrics;
import io.prometheus.client.Gauge;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

import static io.jenkins.plugins.collector.config.Constant.METRICS_LABEL_NAME_ARRAY;
import static io.jenkins.plugins.collector.config.Constant.METRICS_NAMESPACE;
import static io.jenkins.plugins.collector.config.Constant.METRICS_NAME_PREFIX;
import static io.jenkins.plugins.collector.config.Constant.METRICS_SUBSYSTEM;

@Extension
public class Context extends AbstractModule {

  @Override
  public void configure() {
    CustomizeMetrics metrics = new CustomizeMetrics();
    bind(PrometheusMetrics.class).toInstance(new DefaultPrometheusMetrics(metrics));
    bind(CustomizeMetrics.class).annotatedWith(Names.named("customizeMetrics")).toInstance(metrics);
    requestStaticInjection(BuildMetricsCalculator.class);
  }

  @Provides
  @Singleton
  @Named("successBuildHandlerSupplier")
  Supplier<BiConsumer<String[], Run>> successBuildHandlerSupplier(@Named("customizeMetrics") CustomizeMetrics customizeMetrics) {
    Gauge leadTimeMetrics = Gauge.build()
        .name(METRICS_NAME_PREFIX + "_merge_lead_time")
        .subsystem(METRICS_SUBSYSTEM).namespace(METRICS_NAMESPACE)
        .labelNames(METRICS_LABEL_NAME_ARRAY)
        .help("Code Merge Lead Time in milliseconds")
        .create();

    Gauge recoverTimeMetrics = Gauge.build()
        .name(METRICS_NAME_PREFIX + "_failed_build_recovery_time")
        .subsystem(METRICS_SUBSYSTEM).namespace(METRICS_NAMESPACE)
        .labelNames(METRICS_LABEL_NAME_ARRAY)
        .help("Failed Build Recovery Time in milliseconds")
        .create();

    customizeMetrics.addCollector(leadTimeMetrics);
    customizeMetrics.addCollector(recoverTimeMetrics);

    return () -> new LeadTimeHandler(leadTimeMetrics)
        .andThen(new RecoverTimeHandler(recoverTimeMetrics));
  }

  @Provides
  @Singleton
  @Named("buildInfoHandlerSupplier")
  Supplier<BiConsumer<String[], Run>> buildInfoHandlerSupplier(@Named("customizeMetrics") CustomizeMetrics customizeMetrics) {
    Gauge buildDurationMetrics = Gauge.build()
        .name(METRICS_NAME_PREFIX + "_last_build_duration_in_milliseconds")
        .subsystem(METRICS_SUBSYSTEM).namespace(METRICS_NAMESPACE)
        .labelNames(METRICS_LABEL_NAME_ARRAY)
        .help("One build duration in milliseconds")
        .create();

    Gauge buildStartTimeMetrics = Gauge.build()
        .name(METRICS_NAME_PREFIX + "_last_build_start_timestamp")
        .subsystem(METRICS_SUBSYSTEM).namespace(METRICS_NAMESPACE)
        .labelNames(METRICS_LABEL_NAME_ARRAY)
        .help("One build start timestamp")
        .create();

    customizeMetrics.addCollector(buildDurationMetrics);
    customizeMetrics.addCollector(buildStartTimeMetrics);

    return () -> new BuildInfoHandler(buildDurationMetrics, buildStartTimeMetrics);
  }

}
