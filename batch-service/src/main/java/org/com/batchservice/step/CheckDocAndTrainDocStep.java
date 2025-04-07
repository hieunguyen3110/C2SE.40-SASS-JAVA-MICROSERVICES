package org.com.batchservice.step;

import lombok.RequiredArgsConstructor;
import org.com.batchservice.dto.response.RatingDto;
import org.com.batchservice.processor.CheckDocAndTrainDocProcessorCustom;
import org.com.batchservice.processor.CheckingRatingContentProcessorCustom;
import org.com.batchservice.reader.CheckDocAndTrainDocReaderCustom;
import org.com.batchservice.reader.CheckingRatingContentReaderCustom;
import org.com.batchservice.writter.CheckDocAndTrainDocWriterCustom;
import org.com.batchservice.writter.CheckingRatingContentWriterCustom;
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
public class CheckDocAndTrainDocStep {
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    @Bean
    public Step checkingDocAndTrainDocStep(ItemReader<Object> checkingDocAndTrainDocReader,
                                       ItemProcessor<Object,Object> checkingDocAndTrainDocProcessor,
                                       ItemWriter<Object> checkingDocAndTrainDocWriter)
    {
        return new StepBuilder("CheckDocAndTrainDocStep", jobRepository)
                .<Object, Object>chunk(10, transactionManager)
                .reader(checkingDocAndTrainDocReader)
                .processor(checkingDocAndTrainDocProcessor)
                .writer(checkingDocAndTrainDocWriter)
                .build();
    }
    @Bean
    @StepScope
    public ItemReader<Object> checkingDocAndTrainDocReader(CheckDocAndTrainDocReaderCustom readerCustom){
        return readerCustom;
    }
    @Bean
    @StepScope
    public ItemProcessor<Object,Object> checkingDocAndTrainDocProcessor(CheckDocAndTrainDocProcessorCustom processorCustom){
        return processorCustom;
    }
    @Bean
    @StepScope
    public ItemWriter<Object> checkingDocAndTrainDocWriter(CheckDocAndTrainDocWriterCustom writerCustom){
        return writerCustom;
    }
}
