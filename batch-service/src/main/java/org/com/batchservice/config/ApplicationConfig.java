package org.com.batchservice.config;

import org.com.batchservice.helpers.SensitiveWordChecker;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationConfig {
    @Bean
    public SensitiveWordChecker sensitiveWordChecker(){
        return new SensitiveWordChecker();
    }
}
