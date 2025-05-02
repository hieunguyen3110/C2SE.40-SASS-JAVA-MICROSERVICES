package org.com.notificationservice.handler;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.com.notificationservice.dto.request.NotificationEventRequest;
import org.com.notificationservice.dto.response.AccountDto;
import org.com.notificationservice.dto.response.GroupNotification;
import org.com.notificationservice.dto.response.NotificationDto;
import org.com.notificationservice.entity.Notification;
import org.com.notificationservice.mapper.NotificationMapper;
import org.com.notificationservice.repository.NotificationRepository;
import org.com.notificationservice.service.RedisService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class KafkaConsumerHandler {
    private final NotificationRepository notificationRepository;
    private final RedisService redisService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final KafkaTemplate<String, String> kafkaTemplate;

    private Notification convertToNotification(NotificationEventRequest request){
        return Notification.builder()
                .accountId(request.getAccountId())
                .isRead(false)
                .message(request.getMessage())
                .type(request.getType())
                .isSaved(false)
                .deletedFlag(false)
                .build();
    }

    @KafkaListener(topics = "file-upload-topic", groupId = "notification-group")
    public void listenFileUploadEvent(ConsumerRecord<String, String> record) throws Exception {
        try{
            String followerId = record.key(); // ID của follower
            String messageJson = record.value(); // Thông báo
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());
            objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS); // Sử dụng định dạng ISO
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false); // Bỏ qua thuộc tính không mong muốn
            NotificationEventRequest notificationEventRequest = objectMapper.readValue(messageJson, NotificationEventRequest.class);
            Notification notification= convertToNotification(notificationEventRequest);
            notification= notificationRepository.save(notification);
            NotificationDto notificationDto= NotificationMapper.mapToNotificationDto(notification);
            String json= objectMapper.writeValueAsString(notificationDto);
            kafkaTemplate.send("follow-ws-topic",followerId,json);
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
            NotificationEventRequest notificationEventRequest = objectMapper.readValue(messageJson, NotificationEventRequest.class);
            Notification notification= convertToNotification(notificationEventRequest);
            notification= notificationRepository.save(notification);
            NotificationDto notificationDto= NotificationMapper.mapToNotificationDto(notification);
            String json= objectMapper.writeValueAsString(notificationDto);
            kafkaTemplate.send("file-upload-ws-topic",followerId,json);
        }catch (Exception e){
            throw new Exception(e.getMessage());
        }
    }

    @KafkaListener(topics = "e-learning-topic", groupId = "notification-group")
    public void listenBatchJobEvent(ConsumerRecord<String, String> record) throws Exception {
        try{
            String accountId = record.key();
            String messageJson = record.value();
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());
            objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS); // Sử dụng định dạng ISO
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false); // Bỏ qua thuộc tính không mong muốn
            NotificationEventRequest notificationEventRequest = objectMapper.readValue(messageJson, NotificationEventRequest.class);
            Notification notification= convertToNotification(notificationEventRequest);
            notification= notificationRepository.save(notification);
            NotificationDto notificationDto= NotificationMapper.mapToNotificationDto(notification);
            String json= objectMapper.writeValueAsString(notificationDto);
            kafkaTemplate.send("e-learning-ws-topic",accountId,json);
        }catch (Exception e){
            throw new Exception(e.getMessage());
        }
    }

    @KafkaListener(topics = "save-notification-topic", groupId = "notification-group")
    public void listenJoinGroupEvent(ConsumerRecord<String, String> record) throws Exception {
        try{
            String accountId = record.key();
            String messageJson = record.value();
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());
            objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS); // Sử dụng định dạng ISO
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false); // Bỏ qua thuộc tính không mong muốn
            NotificationEventRequest notificationEventRequest = objectMapper.readValue(messageJson, NotificationEventRequest.class);
            Notification notification= convertToNotification(notificationEventRequest);
            notification= notificationRepository.save(notification);
            NotificationDto notificationDto= NotificationMapper.mapToNotificationDto(notification);
            String json= objectMapper.writeValueAsString(notificationDto);
            kafkaTemplate.send("join-group-ws-topic",accountId,json);
        }catch (Exception e){
            throw new Exception(e.getMessage());
        }
    }

    @KafkaListener(topics = "user-update-topic", groupId = "notification-group")
    public void listenUserUpdateEvent(ConsumerRecord<String, String> record) throws Exception {
        try{
            String token = record.key();
            String accountJson = record.value();
            AccountDto accountDto = objectMapper.readValue(accountJson,AccountDto.class);
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());
            objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS); // Sử dụng định dạng ISO
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false); // Bỏ qua thuộc tính không mong muốn
            String json= objectMapper.writeValueAsString(accountDto);
            redisService.updateData(token,json);
            String accountKey = "account:" + accountDto.getAccountId();
            redisService.updateData(accountKey,json);
        }catch (Exception e){
            throw new Exception(e.getMessage());
        }
    }

    @KafkaListener(topics = "study-group-topic", groupId = "notification-group")
    public void listenUserDeleteEvent(ConsumerRecord<String, Object> record) throws Exception {
        try{
            objectMapper.registerModule(new JavaTimeModule());
            objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

            String userId = record.key();
            Object notificationData = record.value();

            GroupNotification groupNotification = objectMapper.convertValue(notificationData, GroupNotification.class);

            String destination = "/topic/group-notification/" + groupNotification.getGroupId();
            kafkaTemplate.send("study-group-ws-topic",userId,"");
        }catch (Exception e){
            throw new Exception(e.getMessage());
        }
    }
}
