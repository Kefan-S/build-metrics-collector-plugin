package io.jenkins.plugins.collector.context;

import com.google.inject.AbstractModule;
import hudson.Extension;
import io.jenkins.plugins.collector.service.DefaultPrometheusMetrics;
import io.jenkins.plugins.collector.service.PrometheusMetrics;

@Extension
public class Context extends AbstractModule {

  @Override
  public void configure() {
    bind(PrometheusMetrics.class).to(DefaultPrometheusMetrics.class).in(com.google.inject.Singleton.class);
  }

}
