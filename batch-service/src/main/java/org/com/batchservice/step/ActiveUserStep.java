package org.com.batchservice.step;

import lombok.RequiredArgsConstructor;
import org.com.batchservice.dto.response.AccountDto;
import org.com.batchservice.dto.response.DocumentDto;
import org.com.batchservice.processor.ActiveUserProcessorCustom;
import org.com.batchservice.processor.CheckDocAndTrainDocProcessorCustom;
import org.com.batchservice.reader.ActiveUserReaderCustom;
import org.com.batchservice.reader.CheckDocAndTrainDocReaderCustom;
import org.com.batchservice.writter.ActiveUserWriterCustom;
import org.com.batchservice.writter.CheckDocAndTrainDocWriterCustom;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@RequiredArgsConstructor
public class ActiveUserStep {
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    @Bean
    public Step activeUserStepHandler(ItemReader<AccountDto> activeUserReader,
                                           ItemProcessor<AccountDto,AccountDto> activeUserProcessor,
                                           ItemWriter<AccountDto> activeUserWriter)
    {
        return new StepBuilder("ActiveUserStep", jobRepository)
                .<AccountDto, AccountDto>chunk(5, transactionManager)
                .reader(activeUserReader)
                .processor(activeUserProcessor)
                .writer(activeUserWriter)
                .build();
    }
    @Bean
    @StepScope
    public ItemReader<AccountDto> activeUserReader(ActiveUserReaderCustom readerCustom){
        return readerCustom;
    }
    @Bean
    @StepScope
    public ItemProcessor<AccountDto,AccountDto> activeUserProcessor(ActiveUserProcessorCustom processorCustom){
        return processorCustom;
    }
    @Bean
    @StepScope
    public ItemWriter<AccountDto> activeUserWriter(ActiveUserWriterCustom writerCustom){
        return writerCustom;
    }
}
