package org.com.batchservice.step;

import lombok.RequiredArgsConstructor;
import org.com.batchservice.dto.response.MultipleNewData;
import org.com.batchservice.processor.CollectDataProcessorCustom;
import org.com.batchservice.reader.CollectDataReaderCustom;
import org.com.batchservice.writter.CollectDataWriterCustom;
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
public class CollectDataStep {
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    @Bean
    public Step collectDataStepHandler(ItemReader<MultipleNewData> collectDataReader,
                                       ItemProcessor<MultipleNewData,MultipleNewData> collectDataProcessor,
                                       ItemWriter<MultipleNewData> collectDataWriter)
    {
        return new StepBuilder("collectDataStep", jobRepository)
                .<MultipleNewData, MultipleNewData>chunk(4, transactionManager)
                .reader(collectDataReader)
                .processor(collectDataProcessor)
                .writer(collectDataWriter)
                .build();
    }
    @Bean
    @StepScope
    public ItemReader<MultipleNewData> collectDataReader(CollectDataReaderCustom readerCustom){
        return readerCustom;
    }
    @Bean
    @StepScope
    public ItemProcessor<MultipleNewData,MultipleNewData> collectDataProcessor(CollectDataProcessorCustom processorCustom){
        return processorCustom;
    }
    @Bean
    @StepScope
    public ItemWriter<MultipleNewData> collectDataWriter(CollectDataWriterCustom writerCustom){
        return writerCustom;
    }
}
