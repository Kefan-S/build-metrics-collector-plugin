package io.jenkins.plugins.collector.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

public class HttpResponseUtil {
  public static HttpResponse generateHttpResponse(Object responseBody) {
    return (StaplerRequest request, StaplerResponse response, Object node) -> {
      response.setStatus(StaplerResponse.SC_OK);
      response.setContentType("application/json; charset=UTF-8");
      response.addHeader("Access-Control-Allow-Origin", "*");
      response.addHeader("Cache-Control", "must-revalidate,no-cache,no-store");
      response.getWriter().write(new ObjectMapper().writeValueAsString(responseBody));
    };
  }

}
