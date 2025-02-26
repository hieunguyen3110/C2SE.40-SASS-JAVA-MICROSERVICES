package com.capstone1.sasscapstone1.util;

import com.capstone1.sasscapstone1.dto.NotificationDto.NotificationDto;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KafkaConsumer {
    private final SimpMessagingTemplate simpMessagingTemplate;

    @KafkaListener(topics = "file-upload-topic", groupId = "notification-group")
    public void listenFileUploadEvent(ConsumerRecord<String, String> record) throws Exception {
       try{
           String followerId = record.key(); // ID của follower
           String messageJson = record.value(); // Thông báo
           ObjectMapper objectMapper = new ObjectMapper();
           objectMapper.registerModule(new JavaTimeModule());
           objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS); // Sử dụng định dạng ISO
           objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false); // Bỏ qua thuộc tính không mong muốn
           NotificationDto notificationDto = objectMapper.readValue(messageJson, NotificationDto.class);
           simpMessagingTemplate.convertAndSendToUser(followerId, "/queue/notifications-with-upload", notificationDto);
       }catch (Exception e){
           throw new Exception(e.getMessage());
       }
    }

    @KafkaListener(topics = "follow-topic", groupId = "notification-group")
    public void listenFollowUserEvent(ConsumerRecord<String, String> record) throws Exception {
        try{
            String followerId = record.key(); // ID của follower
            String messageJson = record.value(); // Thông báo
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());
            objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS); // Sử dụng định dạng ISO
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false); // Bỏ qua thuộc tính không mong muốn
            NotificationDto notificationDto = objectMapper.readValue(messageJson, NotificationDto.class);
            simpMessagingTemplate.convertAndSendToUser(followerId, "/queue/notifications-with-follow", notificationDto);
        }catch (Exception e){
            throw new Exception(e.getMessage());
        }
    }
}
