package io.jenkins.plugins.collector.rest;

import com.google.inject.Inject;
import hudson.Extension;
import hudson.model.UnprotectedRootAction;
import hudson.util.HttpResponses;
import io.jenkins.plugins.collector.service.PrometheusMetrics;
import io.prometheus.client.exporter.common.TextFormat;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

@Extension
public class PrometheusAction implements UnprotectedRootAction {

  private PrometheusMetrics prometheusMetrics;

  @Inject
  public void setPrometheusMetrics(PrometheusMetrics prometheusMetrics) {
    this.prometheusMetrics = prometheusMetrics;
  }

  @Override
  public String getIconFileName() {
    return null;
  }

  @Override
  public String getDisplayName() {
    return "Build Metrics Exporter";
  }

  @Override
  public String getUrlName() {
    return "prometheus";
  }

  public HttpResponse doDynamic(StaplerRequest request) {
    if (request.getRestOfPath().equals("")) {
      return prometheusResponse();
    }
    return HttpResponses.notFound();
  }

  private HttpResponse prometheusResponse() {
    return (request, response, node) -> {
      response.setStatus(StaplerResponse.SC_OK);
      response.setContentType(TextFormat.CONTENT_TYPE_004);
      response.addHeader("Cache-Control", "must-revalidate,no-cache,no-store");
      response.getWriter().write(prometheusMetrics.getMetrics());
    };
  }
}
