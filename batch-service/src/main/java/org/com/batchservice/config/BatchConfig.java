package org.com.batchservice.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.*;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class BatchConfig {
    private final Job customCheckingRatingContentJob;
    private final Job customCollectDataJob;
    private final JobLauncher jobLauncher;

    public void runJobCheckDocAndTrainDoc(){
        JobParameters checkingRatingContentParameter= new JobParametersBuilder()
                .addString("ID", "checking_rating_content_"+System.currentTimeMillis())
                .toJobParameters();
        try{
            JobExecution execution= jobLauncher.run(customCheckingRatingContentJob,checkingRatingContentParameter);
            if(execution.getStatus() == BatchStatus.COMPLETED){
                JobParameters collectDataParameter= new JobParametersBuilder()
                        .addString("ID", "collect_data_"+System.currentTimeMillis())
                        .toJobParameters();
                log.info("Starting Job 2: collect data...");
                JobExecution execution1= jobLauncher.run(customCollectDataJob,collectDataParameter);
                log.info("Collect data is status {}",execution1.getStatus());
            }else{
                log.warn("Checking rating content not success, Collect data will not run.");
            }
        }catch (Exception e){
            log.error("Exception: "+ e.getMessage());
        }
    }
    public void runJobCheckingRatingContent(){
        JobParameters checkingRatingContentParameter= new JobParametersBuilder()
                .addString("ID", "checking_rating_content_"+System.currentTimeMillis())
                .toJobParameters();
        try{
            JobExecution execution= jobLauncher.run(customCheckingRatingContentJob,checkingRatingContentParameter);
            if(execution.getStatus() == BatchStatus.COMPLETED){
                JobParameters collectDataParameter= new JobParametersBuilder()
                        .addString("ID", "collect_data_"+System.currentTimeMillis())
                        .toJobParameters();
                log.info("Starting Job 2: collect data...");
                JobExecution execution1= jobLauncher.run(customCollectDataJob,collectDataParameter);
                log.info("Collect data is status {}",execution1.getStatus());
            }else{
                log.warn("Checking rating content not success, Collect data will not run.");
            }
        }catch (Exception e){
            log.error("Exception: "+ e.getMessage());
        }
    }

}
