package org.com.batchservice.config;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@Setter
@Getter
public class JobCompletionNotificationListener implements JobExecutionListener {
    private final JdbcTemplate jdbcTemplate;
    private static final Logger LOGGER = LoggerFactory.getLogger(JobCompletionNotificationListener.class);

    public JobCompletionNotificationListener(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void beforeJob(@NotNull JobExecution jobExecution) {
        LOGGER.info("Job starting...");
        JobExecutionListener.super.beforeJob(jobExecution);
    }

    @Override
    public void afterJob(@NotNull JobExecution jobExecution) {
        if (jobExecution.getStatus() == BatchStatus.COMPLETED) {
            LOGGER.info("!!! JOB FINISHED! Time to verify the results");
        } else {
            LOGGER.error("Job did not complete successfully. Status: {}", jobExecution.getStatus());
        }
        JobExecutionListener.super.afterJob(jobExecution);
    }
}
