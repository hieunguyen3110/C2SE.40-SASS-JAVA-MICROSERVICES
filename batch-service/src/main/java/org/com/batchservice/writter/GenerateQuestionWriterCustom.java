package org.com.batchservice.writter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.com.batchservice.dto.response.QuestionResponse;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class GenerateQuestionWriterCustom implements ItemWriter<List<QuestionResponse>> {

    @Override
    public void write(Chunk<? extends List<QuestionResponse>> chunk) throws Exception {
        try{
            for(List<QuestionResponse> data : chunk){
                //save into database
                log.info("receive data: "+data.toString());
            }
        }catch (Exception e){
            throw new RuntimeException("Failed to run job with exception: "+ e.getMessage());
        }
    }
}
