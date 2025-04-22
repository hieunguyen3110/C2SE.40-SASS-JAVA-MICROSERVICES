package org.com.studygroupservice.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.com.studygroupservice.dto.request.MessageRequest;
import org.com.studygroupservice.entity.Message;
import org.com.studygroupservice.entity.StudyGroup;
import org.com.studygroupservice.enums.ErrorCode;
import org.com.studygroupservice.exception.ApiException;
import org.com.studygroupservice.repository.MessageRepository;
import org.com.studygroupservice.repository.StudyGroupRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaConsumerHandler {
    private final ObjectMapper objectMapper;
    private final StudyGroupRepository studyGroupRepository;
    private final MessageRepository messageRepository;

    @KafkaListener(topics = "send-message-topic", groupId = "websocket-group")
    public void sendNotificationFollowForClient(ConsumerRecord<String, String> records) {
        try{
            String senderId= records.key();
            String json= records.value();
            MessageRequest messageRequest= objectMapper.readValue(json, MessageRequest.class);
            StudyGroup existGroup= studyGroupRepository.findById(messageRequest.getGroupId())
                    .orElseThrow(()->new ApiException(ErrorCode.BAD_REQUEST.getStatusCode().value(),"Study group not exist"));
            Message newMessage= Message.builder()
                    .senderId(Long.parseLong(senderId))
                    .group(existGroup)
                    .content(messageRequest.getContent())
                    .build();
            messageRepository.save(newMessage);
        }catch (JsonProcessingException e){
            log.error("Error: "+ e.getMessage());
        }
    }
}
