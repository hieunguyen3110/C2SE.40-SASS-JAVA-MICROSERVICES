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
public class ActiveUserJob {
    private final JobRepository jobRepository;
    @Bean(name = "customActiveUserJob")
    public Job checkDocAndTrainDocJob(JobCompletionNotificationListener listener, Step activeUserStepHandler){
        return new JobBuilder("activeUserJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .listener(listener)
                .flow(activeUserStepHandler)
                .end()
                .build();
    }
}
