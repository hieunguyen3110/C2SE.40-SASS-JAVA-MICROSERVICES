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
}
