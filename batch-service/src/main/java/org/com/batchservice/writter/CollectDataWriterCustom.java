package org.com.batchservice.writter;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.com.batchservice.dto.response.MultipleNewData;
import org.com.batchservice.repository.RecommendationClient;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
@Setter
@Getter
public class CollectDataWriterCustom implements ItemWriter<MultipleNewData> {
    private final RecommendationClient recommendationClient;
    @Override
    public void write(Chunk<? extends MultipleNewData> chunk) throws Exception {
        for (MultipleNewData data : chunk){
            int statusCode= recommendationClient.loadData(data).getCode();
            if(statusCode==200){
                log.info("Finish load data");
            }else{
                log.error("Have error when load data into RS");
                throw new Exception("Have error when load data into RS");
            }
        }
    }
}
