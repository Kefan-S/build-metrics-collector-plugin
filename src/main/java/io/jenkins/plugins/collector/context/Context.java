package io.jenkins.plugins.collector.context;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.google.inject.name.Names;
import hudson.Extension;
import io.jenkins.plugins.collector.handler.LeadTimeHandler;
import io.jenkins.plugins.collector.handler.RecoverTimeHandler;
import io.jenkins.plugins.collector.service.DefaultPrometheusMetrics;
import io.jenkins.plugins.collector.service.PrometheusMetrics;
import io.prometheus.client.Gauge;

import static io.jenkins.plugins.collector.config.Constant.METRICS_LABEL_NAME_ARRAY;
import static io.jenkins.plugins.collector.config.Constant.METRICS_NAMESPACE;
import static io.jenkins.plugins.collector.config.Constant.METRICS_NAME_PREFIX;
import static io.jenkins.plugins.collector.config.Constant.METRICS_SUBSYSTEM;

@Extension
public class Context extends AbstractModule {

  @Override
  public void configure() {
    bind(PrometheusMetrics.class).to(DefaultPrometheusMetrics.class).in(Singleton.class);
    bind(RecoverTimeHandler.class).toInstance(new RecoverTimeHandler());
    bind(LeadTimeHandler.class).toInstance(new LeadTimeHandler());
    bindGauge("leadTimeGauge", "_merge_lead_time", "Code Merge Lead Time in milliseconds");
    bindGauge("recoverTimeGauge", "_failed_build_recovery_time", "Failed Build Recovery Time in milliseconds");
    bindGauge("startTimeGauge", "_last_build_start_timestamp", "One build start timestamp");
    bindGauge("durationGauge", "_last_build_duration_in_milliseconds", "One build duration in milliseconds");
  }

  private void bindGauge(String name, String nameSuffix, String description) {
    Gauge gauge = Gauge.build()
        .name(METRICS_NAME_PREFIX + nameSuffix)
        .subsystem(METRICS_SUBSYSTEM).namespace(METRICS_NAMESPACE)
        .labelNames(METRICS_LABEL_NAME_ARRAY.toArray(new String[0]))
        .help(description)
        .create();
    bind(Gauge.class).annotatedWith(Names.named(name))
        .toInstance(gauge);
  }
}
