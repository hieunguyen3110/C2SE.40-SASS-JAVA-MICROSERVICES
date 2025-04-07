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
public class CheckDocAndTrainDocJob {
    private final JobRepository jobRepository;
    @Bean(name = "customCheckDocAndTrainDocJob")
    public Job checkDocAndTrainDocJob(JobCompletionNotificationListener listener, Step checkRatingContentStep){
        return new JobBuilder("checkDocAndTrainDocJob", jobRepository).incrementer(new RunIdIncrementer()).listener(listener).flow(checkRatingContentStep).end()
                .build();
    }
}
