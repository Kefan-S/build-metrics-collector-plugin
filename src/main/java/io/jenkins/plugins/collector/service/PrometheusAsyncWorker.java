package io.jenkins.plugins.collector.service;

import com.google.inject.Inject;
import hudson.Extension;
import hudson.model.AsyncPeriodicWork;
import hudson.model.TaskListener;
import io.jenkins.plugins.collector.config.PrometheusConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

@Extension
public class PrometheusAsyncWorker extends AsyncPeriodicWork {

    private static final Logger logger = LoggerFactory.getLogger(PrometheusAsyncWorker.class);

    private PrometheusMetrics prometheusMetrics;

    public PrometheusAsyncWorker() {
        super("prometheus_async_worker");
    }

    @Inject
    public void setPrometheusMetrics(PrometheusMetrics prometheusMetrics) {
        this.prometheusMetrics = prometheusMetrics;
    }

    @Override
    public long getRecurrencePeriod() {
        long collectingMetricsPeriodInSeconds = TimeUnit.SECONDS.toMillis(PrometheusConfiguration.get().getCollectingMetricsPeriodInSeconds());
        logger.debug("Setting recurrence period to {} in seconds", PrometheusConfiguration.get().getCollectingMetricsPeriodInSeconds());
        return collectingMetricsPeriodInSeconds;
    }

    @Override
    public void execute(TaskListener taskListener) {
        logger.debug("Collecting prometheus metrics");
        prometheusMetrics.collectMetrics();
        logger.debug("Prometheus metrics collected successfully");
    }

}

