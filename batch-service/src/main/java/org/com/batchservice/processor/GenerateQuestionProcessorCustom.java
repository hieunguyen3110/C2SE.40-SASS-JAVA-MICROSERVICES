package org.com.batchservice.processor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.com.batchservice.dto.response.DocumentDto;
import org.com.batchservice.dto.response.QuestionResponse;
import org.com.batchservice.repository.ChatbotClient;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class GenerateQuestionProcessorCustom implements ItemProcessor<DocumentDto, List<QuestionResponse>> {
    private final ChatbotClient chatbotClient;
    @Override
    public List<QuestionResponse> process(DocumentDto item) throws Exception {
        try{
            return chatbotClient.generateQuestion(List.of(item.getDocId())).getData();
        }catch (Exception e){
            log.error("Error when try generate question:" + e);
            throw new Exception(e);
        }
    }
}
