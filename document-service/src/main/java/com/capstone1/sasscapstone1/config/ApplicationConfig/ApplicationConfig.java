package com.capstone1.sasscapstone1.config.ApplicationConfig;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.client.RestTemplate;


import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
@EnableAsync
public class ApplicationConfig {

    @Bean(name = "asyncExecutor" )
    public Executor asyncExecutor() throws Exception {
        try{
            ThreadPoolTaskExecutor executor= new ThreadPoolTaskExecutor();
            executor.setCorePoolSize(5);
            executor.setMaxPoolSize(10);
            executor.setQueueCapacity(20);
            executor.initialize();
            return executor;
        }catch (Exception e){
            throw new Exception(e.getMessage());
        }
    }

    @Bean
    public RestTemplate restTemplate(){
        return new RestTemplate();
    }
}
