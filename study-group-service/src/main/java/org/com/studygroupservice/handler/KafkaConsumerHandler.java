package org.com.studygroupservice.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.com.studygroupservice.dto.request.MessageRequest;
import org.com.studygroupservice.dto.response.MessageResponse;
import org.com.studygroupservice.entity.Message;
import org.com.studygroupservice.entity.StudyGroup;
import org.com.studygroupservice.enums.ErrorCode;
import org.com.studygroupservice.enums.MessageType;
import org.com.studygroupservice.exception.ApiException;
import org.com.studygroupservice.repository.MessageRepository;
import org.com.studygroupservice.repository.StudyGroupRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaConsumerHandler {
    private final ObjectMapper objectMapper;
    private final StudyGroupRepository studyGroupRepository;
    private final MessageRepository messageRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    @Value("${spring.utc.time}")
    private String timeUtc;

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
                    .documentLink(false)
                    .isPinned(false)
                    .messageType(MessageType.valueOf(messageRequest.getMessageType()))
                    .documentId(messageRequest.getDocumentId())
                    .docFilePath(messageRequest.getDocFilePath())
                    .documentName(messageRequest.getDocumentName())
                    .build();
            LocalDateTime timeStamp= LocalDateTime.parse(messageRequest.getTimestamp());
            ZonedDateTime utcZdt = timeStamp.atZone(ZoneId.of("UTC"));
            ZonedDateTime hanoiZdt = utcZdt.withZoneSameInstant(ZoneId.of("Asia/Ho_Chi_Minh"));
            newMessage.setCreatedAt(hanoiZdt.toLocalDateTime());
            newMessage.setUpdatedAt(hanoiZdt.toLocalDateTime());
            Message message= messageRepository.save(newMessage);
            String splitUsername;
            if(messageRequest.getUsername().contains("@")){
                splitUsername = messageRequest.getUsername().split("@")[0];
            }else{
                splitUsername = messageRequest.getUsername();
            }
            MessageResponse response= MessageResponse.builder()
                    .senderId(Long.parseLong(senderId))
                    .content(message.getContent())
                    .groupId(messageRequest.getGroupId())
                    .profilePicture(messageRequest.getProfilePicture())
                    .username(splitUsername)
                    .messageId(message.getId())
                    .timestamp(hanoiZdt.toLocalDateTime().toString())
                    .documentId(message.getDocumentId())
                    .documentName(message.getDocumentName())
                    .docFilePath(message.getDocFilePath())
                    .messageType(message.getMessageType().getMessageType())
                    .build();
            String responseJson= objectMapper.writeValueAsString(response);
            kafkaTemplate.send("send-message-ws-topic",response.getGroupId().toString(),responseJson);
        }catch (JsonProcessingException e){
            log.error("Error: "+ e.getMessage());
        }
    }
}
