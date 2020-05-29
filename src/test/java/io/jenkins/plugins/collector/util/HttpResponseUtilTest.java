package io.jenkins.plugins.collector.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jenkins.plugins.collector.model.BuildInfoResponse;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import org.junit.Test;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.StaplerResponse;
import org.mockito.Mockito;

public class HttpResponseUtilTest {

  @Test
  public void should_return_json_httpResponse_when_given_build_info_list() throws IOException, ServletException {
    BuildInfoResponse buildInfoResponse = BuildInfoResponse.builder().build();
    StaplerResponse staplerResponse = Mockito.mock(StaplerResponse.class);
    PrintWriter printWriter = Mockito.mock(PrintWriter.class);
    Mockito.when(staplerResponse.getWriter()).thenReturn(printWriter);

    HttpResponse httpResponse = HttpResponseUtil.generateHttpResponse(buildInfoResponse);
    httpResponse.generateResponse(null, staplerResponse, null);
    Mockito.verify(printWriter, Mockito.times(1)).write(new ObjectMapper().writeValueAsString(buildInfoResponse));
  }

}