package org.com.batchservice.processor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.com.batchservice.dto.response.DocumentDto;
import org.com.batchservice.dto.response.QuestionDto;
import org.com.batchservice.dto.response.QuestionResponse;
import org.com.batchservice.repository.ChatbotClient;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class GenerateQuestionProcessorCustom implements ItemProcessor<DocumentDto, Map<Long,List<QuestionDto>>> {
    private final ChatbotClient chatbotClient;
    @Override
    public Map<Long,List<QuestionDto>> process(DocumentDto item) throws Exception {
        try{
            List<QuestionResponse> questionResponses= chatbotClient.generateQuestion(List.of(item.getDocId())).getData();
            List<QuestionDto> questionDtos= questionResponses.stream()
                    .map(question->QuestionDto.builder()
                            .correctAnswer(question.getCorrect_answer())
                            .options(question.getOptions())
                            .question(question.getQuestion())
                            .build())
                    .toList();
            Map<Long,List<QuestionDto>> questionMap= new HashMap<>();
            questionMap.put(item.getSubjectId(),questionDtos);
            return questionMap;
        }catch (Exception e){
            log.error("Error when try generate question:" + e);
            throw new Exception(e);
        }
    }
}
