package org.com.batchservice.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class QuartzJob implements Job {
    private final BatchConfig batchConfig;
    @Override
    public void execute(JobExecutionContext jobExecutionContext) {
        log.info("Starting Quartz job to launch Spring batch job...");
        batchConfig.runJobCheckingRatingContent();
    }
}
