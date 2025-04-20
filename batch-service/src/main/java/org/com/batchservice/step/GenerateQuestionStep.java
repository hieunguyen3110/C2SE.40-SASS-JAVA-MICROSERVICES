package org.com.batchservice.step;

import lombok.RequiredArgsConstructor;
import org.com.batchservice.dto.response.DocumentDto;
import org.com.batchservice.dto.response.QuestionResponse;
import org.com.batchservice.processor.GenerateQuestionProcessorCustom;
import org.com.batchservice.reader.GenerateQuestionReaderCustom;
import org.com.batchservice.writter.GenerateQuestionWriterCustom;
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

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class GenerateQuestionStep {
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    @Bean
    public Step generateQuestionStepHandler(ItemReader<DocumentDto> generateQuestionReader,
                                       ItemProcessor<DocumentDto,List<QuestionResponse>> generateQuestionProcessor,
                                       ItemWriter<List<QuestionResponse>> generateQuestionWriter)
    {
        return new StepBuilder("generateQuestionStep", jobRepository)
                .<DocumentDto, List<QuestionResponse>>chunk(10, transactionManager)
                .reader(generateQuestionReader)
                .processor(generateQuestionProcessor)
                .writer(generateQuestionWriter)
                .build();
    }
    @Bean
    @StepScope
    public ItemReader<DocumentDto> generateQuestionReader(GenerateQuestionReaderCustom readerCustom){
        return readerCustom;
    }
    @Bean
    @StepScope
    public ItemProcessor<DocumentDto, List<QuestionResponse>> generateQuestionProcessor(GenerateQuestionProcessorCustom processorCustom){
        return processorCustom;
    }
    @Bean
    @StepScope
    public ItemWriter<List<QuestionResponse>> generateQuestionWriter(GenerateQuestionWriterCustom writerCustom){
        return writerCustom;
    }
}
