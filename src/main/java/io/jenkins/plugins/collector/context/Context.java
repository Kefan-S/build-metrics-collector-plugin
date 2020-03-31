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

import java.util.function.BiConsumer;

@Extension
public class Context extends AbstractModule {

    @Override
    public void configure() {
        bind(PrometheusMetrics.class).to(DefaultPrometheusMetrics.class).in(com.google.inject.Singleton.class);
        bind(BiConsumer.class).annotatedWith(Names.named("buildInfoHandler")).to(BuildInfoHandler.class).in(com.google.inject.Singleton.class);
        requestStaticInjection(BuildMetricsCalculator.class);
    }

    @Provides
    @Singleton
    @Named("successBuildHandler")
    BiConsumer<String[], Run> composeSuccessBuildHandler() {
        return new LeadTimeHandler()
                .andThen(new RecoverTimeHandler());
    }

}
