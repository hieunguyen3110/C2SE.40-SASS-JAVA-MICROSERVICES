package com.capstone1.sasscapstone1.service.KafkaService;

import com.capstone1.sasscapstone1.dto.AccountDto.AccountDto;
import com.capstone1.sasscapstone1.dto.NotificationDto.NotificationDto;
import com.capstone1.sasscapstone1.entity.Notification;
import com.capstone1.sasscapstone1.repository.Follow.FollowRepository;
import com.capstone1.sasscapstone1.repository.Notification.NotificationRepository;
import com.capstone1.sasscapstone1.repository.httpClient.IdentityClient;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class KafkaService {
    private final KafkaTemplate<String,String> kafkaTemplate;
    private final NotificationRepository notificationRepository;
    private final FollowRepository followRepository;
    private final IdentityClient identityClient;

    public void sendNotificationFromUserFollower(String fileName, AccountDto accountFollowing) throws Exception {
        try{
            List<Long> findAllAccountFollower= followRepository.findAllFollowerByFollowingId(accountFollowing.getAccountId());
            String messageAccountFollowing= "Your document has been approved.";
            Notification notificationAccountFollowing= Notification.builder()
                    .accountId(accountFollowing.getAccountId())
                    .isRead(false)
                    .message(messageAccountFollowing)
                    .type("UPLOAD FILE")
                    .isSaved(false)
                    .deletedFlag(false)
                    .build();
            Notification saveNotificationAccountFollowing= notificationRepository.save(notificationAccountFollowing);
            NotificationDto notificationAccountFollowingDto= NotificationDto.builder()
                    .notificationId(saveNotificationAccountFollowing.getNotificationId())
                    .message(saveNotificationAccountFollowing.getMessage())
                    .type(saveNotificationAccountFollowing.getType())
                    .isRead(saveNotificationAccountFollowing.getIsRead())
                    .createdAt(saveNotificationAccountFollowing.getCreatedAt())
                    .build();
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());
            objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            String notificationAccountFollowingJson = objectMapper.writeValueAsString(notificationAccountFollowingDto);
            kafkaTemplate.send("file-upload-topic",String.valueOf(accountFollowing.getAccountId()) ,notificationAccountFollowingJson);
            for(Long followerId : findAllAccountFollower){
                String message= "User "+ accountFollowing.getFirstName()+" "+accountFollowing.getLastName()+" uploaded a new file: "+ fileName;
                AccountDto findAccountById= identityClient.getAccountId(followerId).getData();
                Notification notification= Notification.builder()
                        .accountId(findAccountById.getAccountId())
                        .isRead(false)
                        .message(message)
                        .type("UPLOAD FILE")
                        .isSaved(false)
                        .deletedFlag(false)
                        .build();
                Notification saveNotification= notificationRepository.save(notification);
                NotificationDto notificationDto= NotificationDto.builder()
                        .notificationId(saveNotification.getNotificationId())
                        .message(saveNotification.getMessage())
                        .type(saveNotification.getType())
                        .isRead(saveNotification.getIsRead())
                        .createdAt(saveNotification.getCreatedAt())
                        .build();
                String notificationJson = objectMapper.writeValueAsString(notificationDto);
                kafkaTemplate.send("file-upload-topic",followerId.toString() ,notificationJson);
            }
        }catch (Exception e){
            throw new Exception(e.getMessage());
        }
    }

    public void sendNotificationFromUserFollowing(AccountDto accountFollowing, String name) throws Exception {
        try{
            String message= "User "+ name+" just followed you";
            Notification notification= Notification.builder()
                    .accountId(accountFollowing.getAccountId())
                    .isRead(false)
                    .message(message)
                    .type("FOLLOW")
                    .isSaved(false)
                    .deletedFlag(false)
                    .build();
            Notification saveNotification= notificationRepository.save(notification);
            NotificationDto notificationDto= NotificationDto.builder()
                    .notificationId(saveNotification.getNotificationId())
                    .message(saveNotification.getMessage())
                    .type(saveNotification.getType())
                    .isRead(saveNotification.getIsRead())
                    .createdAt(saveNotification.getCreatedAt())
                    .build();
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());
            objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS); // Sử dụng định dạng ISO
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false); // Bỏ qua thuộc tính không mong muốn
            String notificationJson = objectMapper.writeValueAsString(notificationDto);
            kafkaTemplate.send("follow-topic", String.valueOf(accountFollowing.getAccountId()),notificationJson);
        }catch (Exception e){
            throw new Exception(e.getMessage());
        }
    }

}
