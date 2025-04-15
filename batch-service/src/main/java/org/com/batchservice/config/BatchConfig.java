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
    private final Job customCheckDocAndTrainDocJob;
    private final Job customActiveUserJob;
    private final JobLauncher jobLauncher;

    public void runJobCheckDocAndTrainDoc(){
        JobParameters checkDocAndTrainDocParameter= new JobParametersBuilder()
                .addString("ID", "check_doc_and_train_doc_"+System.currentTimeMillis())
                .toJobParameters();
        try{
            jobLauncher.run(customCheckDocAndTrainDocJob,checkDocAndTrainDocParameter);
        }catch (Exception e){
            log.error("Exception: "+ e.getMessage());
        }
    }
    public void runJobActiveUser(){
        JobParameters activeUserParameter= new JobParametersBuilder()
                .addString("ID", "active_user"+System.currentTimeMillis())
                .toJobParameters();
        try{
            jobLauncher.run(customActiveUserJob,activeUserParameter);
        }catch (Exception e){
            log.error("Exception: "+ e.getMessage());
        }
    }
    public void runJobCheckingRatingContent(){
        JobParameters checkingRatingContentParameter= new JobParametersBuilder()
                .addString("ID", "checking_rating_content_"+System.currentTimeMillis())
                .toJobParameters();
        try{
            jobLauncher.run(customCheckingRatingContentJob,checkingRatingContentParameter);
        }catch (Exception e){
            log.error("Exception: "+ e.getMessage());
        }
    }
    public void runJobCollectData(){
        JobParameters collectDataParameter= new JobParametersBuilder()
                .addString("ID", "collect_data_"+System.currentTimeMillis())
                .toJobParameters();
        try{
            jobLauncher.run(customCollectDataJob,collectDataParameter);
        }catch (Exception e){
            log.error("Exception: "+ e.getMessage());
        }
    }

}
