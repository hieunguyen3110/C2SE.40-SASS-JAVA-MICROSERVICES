package org.com.batchservice.step;

import lombok.RequiredArgsConstructor;
import org.com.batchservice.dto.response.RatingDto;
import org.com.batchservice.processor.CheckingRatingContentProcessorCustom;
import org.com.batchservice.reader.CheckingRatingContentReaderCustom;
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
public class CheckRatingContentStep {
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    @Bean
    public Step checkRatingContentStepHandler(ItemReader<RatingDto> checkingRatingContentReader,
                                       ItemProcessor<RatingDto,RatingDto> checkingRatingContentProcessor,
                                       ItemWriter<RatingDto> checkingRatingContentWriter)
    {
        return new StepBuilder("CheckRatingContentStep", jobRepository)
                .<RatingDto, RatingDto>chunk(4, transactionManager)
                .reader(checkingRatingContentReader)
                .processor(checkingRatingContentProcessor)
                .writer(checkingRatingContentWriter)
                .build();
    }
    @Bean
    @StepScope
    public ItemReader<RatingDto> checkingRatingContentReader(CheckingRatingContentReaderCustom readerCustom){
        return readerCustom;
    }
    @Bean
    @StepScope
    public ItemProcessor<RatingDto,RatingDto> checkingRatingContentProcessor(CheckingRatingContentProcessorCustom processorCustom){
        return processorCustom;
    }
    @Bean
    @StepScope
    public ItemWriter<RatingDto> checkingRatingContentWriter(CheckingRatingContentWriterCustom writerCustom){
        return writerCustom;
    }

}
