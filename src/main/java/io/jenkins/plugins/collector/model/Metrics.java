package io.jenkins.plugins.collector.model;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class Metrics {
    private Long startTime;
    private Long duration;
    private Long leadTime;
    private Long recoverTime;
    private String jenkinsJob;
    private String job;
    private String result;
    private String triggeredBy;

}
