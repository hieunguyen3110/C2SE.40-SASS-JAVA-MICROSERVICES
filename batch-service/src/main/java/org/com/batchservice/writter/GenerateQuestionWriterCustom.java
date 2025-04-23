package org.com.batchservice.writter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.com.batchservice.dto.response.DocumentDto;
import org.com.batchservice.dto.response.QuestionDto;
import org.com.batchservice.reader.GenerateQuestionReaderCustom;
import org.com.batchservice.repository.DocumentClient;
import org.com.batchservice.repository.ElearningClient;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class GenerateQuestionWriterCustom implements ItemWriter<Map<Long,List<QuestionDto>>> {
    private final ElearningClient elearningClient;
    private final DocumentClient documentClient;
    private final GenerateQuestionReaderCustom generateQuestionReaderCustom;

    @Override
    public void write(Chunk<? extends Map<Long,List<QuestionDto>>> chunk) throws Exception {
        try{
            for(Map<Long,List<QuestionDto>> data : chunk){
                //save into database
                for(Map.Entry<Long, List<QuestionDto>> entry : data.entrySet()){
                    log.info("receive data: "+entry.getValue().toString());
                    String response= elearningClient.saveNewQuestion(entry.getValue(),entry.getKey()).getData();
                    log.info("Message: "+ response);
                }
                List<Long> documentDtos= generateQuestionReaderCustom.getDocumentDtos().stream()
                        .map(DocumentDto::getDocId)
                        .toList();
                String message= documentClient.updateGenStatus(documentDtos).getData();
                log.info("Message: "+ message);
            }
        }catch (Exception e){
            throw new RuntimeException("Failed to run job with exception: "+ e.getMessage());
        }
    }
}
