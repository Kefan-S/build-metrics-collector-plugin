package io.jenkins.plugins.collector.service;

import com.google.inject.Inject;
import hudson.security.ACL;
import hudson.security.ACLContext;
import io.jenkins.plugins.collector.model.BuildInfo;
import java.util.List;
import java.util.TimerTask;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AsyncWork extends TimerTask {

  private static final Logger logger = LoggerFactory.getLogger(AsyncWork.class);

  private BuildInfoService buildInfoService;
  private Consumer<List<BuildInfo>> buildInfoConsumer;

  @Inject
  public AsyncWork(Consumer<List<BuildInfo>> buildInfoConsumer, BuildInfoService buildInfoService) {

    this.buildInfoConsumer = buildInfoConsumer;
    this.buildInfoService = buildInfoService;
  }

  @Override
  public void run() {
    try (ACLContext ctx = ACL.as(ACL.SYSTEM)) {
      logger.info("Collecting prometheus metrics");
      List<BuildInfo> buildInfoList = buildInfoService.getAllBuildInfo();
      buildInfoConsumer.accept(buildInfoList);
      logger.info("Prometheus metrics collected successfully");
    }
  }
}
