package org.com.websocketserver.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.com.websocketserver.dto.response.NotificationDto;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;

@Slf4j
@RequiredArgsConstructor
public class KafkaConsumerHandler {
    private final SimpMessagingTemplate simpMessagingTemplate;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "follow-ws-topic", groupId = "websocket-group")
    public void sendNotificationFollowForClient(ConsumerRecord<String, String> records) {
        try{
            String followerId= records.key();
            String json= records.value();
            NotificationDto notificationDto= objectMapper.readValue(json,NotificationDto.class);
            simpMessagingTemplate.convertAndSendToUser(followerId, "/queue/notifications-with-follow", notificationDto);
        }catch (JsonProcessingException e){
            log.error("Error: "+ e.getMessage());
        }
    }

    @KafkaListener(topics = "file-upload-ws-topic", groupId = "websocket-group")
    public void sendNotificationFileUploadForClient(ConsumerRecord<String, String> records) {
        try{
            String followerId= records.key();
            String json= records.value();
            NotificationDto notificationDto= objectMapper.readValue(json,NotificationDto.class);
            simpMessagingTemplate.convertAndSendToUser(followerId, "/queue/notifications-with-upload", notificationDto);
        }catch (JsonProcessingException e){
            log.error("Error: "+ e.getMessage());
        }
    }

    @KafkaListener(topics = "study-group-ws-topic", groupId = "websocket-group")
    public void sendNotificationStudyGroupForClient(ConsumerRecord<String, String> records) {
        try {
            if (records.key() == null || records.value() == null) {
                log.error("Received null key or value from Kafka");
                return;
            }

            String followerId = records.key();
            String json = records.value();
            NotificationDto notificationDto = objectMapper.readValue(json, NotificationDto.class);

            if (notificationDto != null) {
                simpMessagingTemplate.convertAndSendToUser(
                        followerId, "/queue/notifications-with-studygroup", notificationDto
                );
                log.info("Sent WebSocket notification to user: {}", followerId);
            } else {
                log.error("NotificationDto is null after parsing JSON");
            }
        } catch (JsonProcessingException e) {
            log.error("Error parsing JSON: {}", e.getMessage());
        }
    }

    @KafkaListener(topics = "chat-ws-topic", groupId = "websocket-group")
    public void sendNotificationChatForClient(ConsumerRecord<String, String> records) {
        try {
            if (records.key() == null || records.value() == null) {
                log.error("Received null key or value from Kafka. Key: {}, Value: {}", records.key(), records.value());
                return;
            }

            String groupId = records.key();
            String json = records.value();

            if (groupId.isBlank()) {
                log.warn("Received blank groupId. Skipping message.");
                return;
            }

            NotificationDto notificationDto = objectMapper.readValue(json, NotificationDto.class);

            if (notificationDto != null) {
                simpMessagingTemplate.convertAndSendToUser(
                        groupId, "/queue/notifications-with-chat", notificationDto
                );
                log.info("Sent WebSocket notification to group '{}': {}", groupId, notificationDto);
            } else {
                log.error("NotificationDto is null after parsing JSON: {}", json);
            }
        } catch (JsonProcessingException e) {
            log.error("Failed to parse JSON: {}", e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error in Kafka listener: {}", e.getMessage(), e);
        }
    }


}
