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
public class StudentLearnTrendAnalyzeJob {
    private final JobRepository jobRepository;
    @Bean(name = "customStudentLearnTrendAnalyzeJob")
    public Job studentLearnTrendAnalyzeJob(JobCompletionNotificationListener listener, Step studentLearnTrendAnalyzeStepHandler){
        return new JobBuilder("studentLearnTrendAnalyzeJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .listener(listener)
                .flow(studentLearnTrendAnalyzeStepHandler)
                .end()
                .build();
    }
}
