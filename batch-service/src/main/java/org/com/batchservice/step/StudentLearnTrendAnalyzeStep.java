package org.com.batchservice.step;

import lombok.RequiredArgsConstructor;
import org.com.batchservice.dto.response.AnalyzeData;
import org.com.batchservice.dto.response.MessageAnalyze;
import org.com.batchservice.processor.StudentLearnTrendAnalyzeProcessorCustom;
import org.com.batchservice.reader.StudentLearnTrendAnalyzeReaderCustom;
import org.com.batchservice.writter.StudentLearnTrendAnalyzeWriterCustom;
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
public class StudentLearnTrendAnalyzeStep {
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    @Bean
    public Step studentLearnTrendAnalyzeStepHandler(ItemReader<AnalyzeData> studentLearnTrendAnalyzeReader,
                                       ItemProcessor<AnalyzeData,MessageAnalyze> studentLearnTrendAnalyzeProcessor,
                                       ItemWriter<MessageAnalyze> studentLearnTrendAnalyzeWriter)
    {
        return new StepBuilder("studentLearnTrendAnalyzeStep", jobRepository)
                .<AnalyzeData, MessageAnalyze>chunk(4, transactionManager)
                .reader(studentLearnTrendAnalyzeReader)
                .processor(studentLearnTrendAnalyzeProcessor)
                .writer(studentLearnTrendAnalyzeWriter)
                .build();
    }
    @Bean
    @StepScope
    public ItemReader<AnalyzeData> studentLearnTrendAnalyzeReader(StudentLearnTrendAnalyzeReaderCustom readerCustom){
        return readerCustom;
    }
    @Bean
    @StepScope
    public ItemProcessor<AnalyzeData,MessageAnalyze> studentLearnTrendAnalyzeProcessor(StudentLearnTrendAnalyzeProcessorCustom processorCustom){
        return processorCustom;
    }
    @Bean
    @StepScope
    public ItemWriter<MessageAnalyze> studentLearnTrendAnalyzeWriter(StudentLearnTrendAnalyzeWriterCustom writerCustom){
        return writerCustom;
    }
}
