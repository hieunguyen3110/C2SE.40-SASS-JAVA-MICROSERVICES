package org.com.batchservice.job;

import lombok.RequiredArgsConstructor;
import org.com.batchservice.config.JobCompletionNotificationListener;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class CheckingRatingContentJob {
    private final JobRepository jobRepository;
    @Bean(name = "customCheckingRatingContentJob")
    public Job checkingRatingContentJob(JobCompletionNotificationListener listener, Step checkingDocAndTrainDocStep){
        return new JobBuilder("checkingRatingContentJob", jobRepository).incrementer(new RunIdIncrementer()).listener(listener).flow(checkingDocAndTrainDocStep).end()
                .build();
    }
}
