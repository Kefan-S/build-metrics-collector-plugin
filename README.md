# build-metrics-collector-plugin
## About
Jenkins Build Metrics Collector Plugin expose an endpoint (default `/prometheus`) with metrics where a Prometheus Server can scrape.

The collected data could help to evaluate software delivery and operational performance.

## Presume
1. The plug-in only supports data statistics for each single pipeline, and does not support consolidated data for multiple pipelines.
For example if you want to calculate the data of production environment, just choose the pipeline for production. 

2. For the delivery lead time, we assume that the pipeline will be triggered by the pushed code, so we can use the pipeline start time to calculate the
code delivery time from codebase to environment.

## Environment variables
`COLLECTING_METRICS_PERIOD_IN_SECONDS` Async task period in seconds (Default: `15` seconds), which is used to refresh the metrics data. 

**Notice:** this value should be less than the scrape_interval of the prometheus to ensure the correctness of the metrics data.

## Collected data
* *default_jenkins_builds_last_build_duration_in_milliseconds*: Last Build duration times in milliseconds
* *default_jenkins_builds_last_build_start_time_in_milliseconds*: Last Build start time in milliseconds
* *default_jenkins_builds_last_build_result_code*: Last Build result: 0 represents failed and 1 for success
* *default_jenkins_builds_failed_build_recovery_time*: Failed Build recovery time in milliseconds
* *default_jenkins_builds_merge_lead_time*: Code delivery time from codebase to environment in milliseconds
* *default_jenkins_builds_success_build_total_count*:
* *default_jenkins_builds_failed_build_total_count*:
* *default_jenkins_builds_build_total_count*:

## Prometheus query example

Deployment Frequency In One Day:
```
count_over_time(default_jenkins_builds_last_build_result_code[1d])
```

Average Failure Rate In One Day:
```
 1 - (sum_over_time(default_jenkins_builds_last_build_result_code[1d])/count_over_time(default_jenkins_builds_last_build_result_code[1d]))
```

Average Lead Time In One Day:
```
avg_over_time(default_jenkins_builds_merge_lead_time[1d])
```

Average Recovery Time In One Day:
```
avg_over_time(default_jenkins_builds_failed_build_recovery_time[1d])
```