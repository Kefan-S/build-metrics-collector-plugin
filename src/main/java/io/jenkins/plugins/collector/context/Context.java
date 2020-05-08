package io.jenkins.plugins.collector.context;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Names;
import hudson.Extension;
import io.jenkins.plugins.collector.model.BuildInfo;
import io.jenkins.plugins.collector.consumer.prometheus.PrometheusConsumer;
import io.jenkins.plugins.collector.consumer.jenkins.JenkinsStorageConsumer;
import io.jenkins.plugins.collector.service.LeadTimeCalculate;
import io.jenkins.plugins.collector.consumer.prometheus.PrometheusMetrics;
import io.jenkins.plugins.collector.service.RecoverTimeCalculate;
import io.prometheus.client.Gauge;
import java.util.List;
import java.util.function.Consumer;
import jenkins.model.Jenkins;

import static io.jenkins.plugins.collector.config.Constant.METRICS_LABEL_NAME_ARRAY;
import static io.jenkins.plugins.collector.config.Constant.METRICS_NAMESPACE;
import static io.jenkins.plugins.collector.config.Constant.METRICS_NAME_PREFIX;
import static io.jenkins.plugins.collector.config.Constant.METRICS_SUBSYSTEM;

@Extension
public class Context extends AbstractModule {

  @Override
  public void configure() {
    bind(PrometheusMetrics.class).to(PrometheusConsumer.class).in(Singleton.class);
    bind(RecoverTimeCalculate.class).toInstance(new RecoverTimeCalculate());
    bind(LeadTimeCalculate.class).toInstance(new LeadTimeCalculate());
    bindGauge("leadTimeGauge", "_merge_lead_time", "Code Merge Lead Time in milliseconds");
    bindGauge("recoverTimeGauge", "_failed_build_recovery_time", "Failed Build Recovery Time in milliseconds");
    bindGauge("startTimeGauge", "_last_build_start_timestamp", "One build start timestamp");
    bindGauge("durationGauge", "_last_build_duration_in_milliseconds", "One build duration in milliseconds");
  }

  @Singleton
  @Provides
  private Consumer<List<BuildInfo>> buildInfoConsumer() {
    return new PrometheusConsumer().andThen(new JenkinsStorageConsumer(Jenkins.get()));
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
