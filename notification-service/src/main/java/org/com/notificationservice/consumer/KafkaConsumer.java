package org.com.notificationservice.consumer;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.com.notificationservice.dto.request.NotificationEventRequest;
import org.com.notificationservice.dto.response.AccountDto;
import org.com.notificationservice.dto.response.NotificationDto;
import org.com.notificationservice.entity.Notification;
import org.com.notificationservice.mapper.NotificationMapper;
import org.com.notificationservice.repository.NotificationRepository;
import org.com.notificationservice.service.RedisService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;

@RequiredArgsConstructor
public class KafkaConsumer {
    private final SimpMessagingTemplate simpMessagingTemplate;
    private final NotificationRepository notificationRepository;
    private final RedisService redisService;

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
            NotificationEventRequest notificationEventRequest = objectMapper.readValue(messageJson, NotificationEventRequest.class);
            Notification notification= convertToNotification(notificationEventRequest);
            notification= notificationRepository.save(notification);
            NotificationDto notificationDto= NotificationMapper.mapToNotificationDto(notification);
            simpMessagingTemplate.convertAndSendToUser(followerId, "/queue/notifications-with-follow", notificationDto);
        }catch (Exception e){
            throw new Exception(e.getMessage());
        }
    }

    @KafkaListener(topics = "user-update-topic", groupId = "notification-group")
    public void listenUserUpdateEvent(ConsumerRecord<String, Object> record) throws Exception {
        try{
            String token = record.key();
            AccountDto accountDto = (AccountDto) record.value();
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());
            objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS); // Sử dụng định dạng ISO
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false); // Bỏ qua thuộc tính không mong muốn
            String json= objectMapper.writeValueAsString(accountDto);
            redisService.updateData(token,json);
        }catch (Exception e){
            throw new Exception(e.getMessage());
        }
    }
}
