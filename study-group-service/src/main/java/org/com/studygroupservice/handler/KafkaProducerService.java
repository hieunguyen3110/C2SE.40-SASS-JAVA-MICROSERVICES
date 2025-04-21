package org.com.studygroupservice.handler;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.com.studygroupservice.dto.request.NotificationEventRequest;
import org.com.studygroupservice.dto.response.AccountDto;
import org.com.studygroupservice.dto.response.MessageEventDto;
import org.com.studygroupservice.dto.response.StudyGroupEventDto;
import org.com.studygroupservice.entity.JoinRequest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaProducerService {
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public void sendStudyGroupEvent(StudyGroupEventDto event) {
        try {
            String message = objectMapper.writeValueAsString(event);
            kafkaTemplate.send("study-group-ws-topic", event.getUserId().toString(), message);
            log.info("Sent study group event to Kafka: {}", message);
        } catch (JsonProcessingException e) {
            log.error("Error converting event to JSON: {}", e.getMessage());
        }
    }

    public void sendJoinRequestEvent(AccountDto accountDto, Long ownerId, List<Long> adminIds, String groupName) throws JsonProcessingException {
        String message= "Người dùng có tên "+accountDto.getLastName()+" đã gửi yêu cầu tham gia vào nhóm "+groupName+".";
        NotificationEventRequest ownerNotification= NotificationEventRequest.builder()
                .accountId(ownerId)
                .message(message)
                .type("JOIN_GROUP")
                .build();
        String ownerJson= objectMapper.writeValueAsString(ownerNotification);
        kafkaTemplate.send("save-notification-topic",ownerId.toString(),ownerJson);
        if(!adminIds.isEmpty()){
            for(Long adminId : adminIds){
                NotificationEventRequest adminNotification= NotificationEventRequest.builder()
                        .accountId(adminId)
                        .message(message)
                        .type("JOIN_GROUP")
                        .build();
                String adminJson= objectMapper.writeValueAsString(adminNotification);
                kafkaTemplate.send("save-notification-topic",adminId.toString(),adminJson);
            }
        }
    }

    public void sendMessageEvent(MessageEventDto event) {
        try {
            String message = objectMapper.writeValueAsString(event);
            kafkaTemplate.send("chat-ws-topic", event.getGroupId().toString(), message);
            log.info("Sent message event to Kafka: {}", message);
        } catch (JsonProcessingException e) {
            log.error("Error converting event to JSON: {}", e.getMessage());
        }
    }
}
