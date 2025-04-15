package org.com.batchservice.config;

import lombok.RequiredArgsConstructor;
import org.quartz.CronScheduleBuilder;
import org.quartz.JobDetail;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

@Configuration
@RequiredArgsConstructor
public class QuartzConfig {
    private final AutowiringSpringBeanJobFactory jobFactory;

    @Bean
    public SchedulerFactoryBean schedulerFactoryBean(Trigger trigger, JobDetail jobDetail, ApplicationContext applicationContext) {
        SchedulerFactoryBean factoryBean = new SchedulerFactoryBean();
        jobFactory.setApplicationContext(applicationContext);
        factoryBean.setJobFactory(jobFactory);
        factoryBean.setTriggers(trigger);
        factoryBean.setJobDetails(jobDetail);
        return factoryBean;
    }

    @Bean
    public Trigger jobTrigger(JobDetail jobDetail) {
        return TriggerBuilder.newTrigger()
                .forJob(jobDetail)
                .withSchedule(CronScheduleBuilder.cronSchedule("0 50 11 * * ?"))
                .build();
    }

    @Bean
    public JobDetailFactoryBean jobDetail() {
        JobDetailFactoryBean factory = new JobDetailFactoryBean();
        factory.setJobClass(QuartzJob.class);
        factory.setDescription("Trigger batch processing job");
        factory.setDurability(true);
        return factory;
    }
}
