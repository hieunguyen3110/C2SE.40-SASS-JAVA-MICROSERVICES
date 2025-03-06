package com.capstone1.sasscapstone1.producer;

import com.capstone1.sasscapstone1.dto.AccountDto.AccountDto;
import com.capstone1.sasscapstone1.enums.KafkaTopic;
import com.capstone1.sasscapstone1.enums.NotificationType;
import com.capstone1.sasscapstone1.repository.Follow.FollowRepository;
import com.capstone1.sasscapstone1.repository.httpClient.IdentityClient;
import com.capstone1.sasscapstone1.request.NotificationEventRequest;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class NotificationProducer {
    private final KafkaTemplate<String,String> kafkaTemplate;
    private final FollowRepository followRepository;
    private final IdentityClient identityClient;

    public void sendNotificationFromUserFollower(String fileName, AccountDto accountFollowing) throws Exception {
        try{
            List<Long> findAllAccountFollower= followRepository.findAllFollowerByFollowingId(accountFollowing.getAccountId());
            String messageAccountFollowing= "Your document has been approved.";
            NotificationEventRequest notificationEventRequest= NotificationEventRequest.builder()
                    .accountId(accountFollowing.getAccountId())
                    .message(messageAccountFollowing)
                    .type(NotificationType.UPLOAD_FILE.getName())
                    .build();
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());
            objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            String notificationAccountFollowingJson = objectMapper.writeValueAsString(notificationEventRequest);
            kafkaTemplate.send(KafkaTopic.UPLOAD_FILE.getName(),String.valueOf(accountFollowing.getAccountId()) ,notificationAccountFollowingJson);
            for(Long followerId : findAllAccountFollower){
                String message= "User "+ accountFollowing.getFirstName()+" "+accountFollowing.getLastName()+" uploaded a new file: "+ fileName;
                AccountDto findAccountById= identityClient.getAccountId(followerId).getData();
                NotificationEventRequest notificationEventRequestFollower= NotificationEventRequest.builder()
                        .accountId(findAccountById.getAccountId())
                        .message(message)
                        .type(NotificationType.UPLOAD_FILE.getName())
                        .build();
                String notificationJson = objectMapper.writeValueAsString(notificationEventRequestFollower);
                kafkaTemplate.send(KafkaTopic.UPLOAD_FILE.getName(),followerId.toString() ,notificationJson);
            }
        }catch (Exception e){
            throw new Exception(e.getMessage());
        }
    }

    public void sendNotificationFromUserFollowing(AccountDto accountFollowing, String name) throws Exception {
        try{
            String message= "User "+ name+" just followed you";
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());
            objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS); // Sử dụng định dạng ISO
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false); // Bỏ qua thuộc tính không mong muốn
            NotificationEventRequest notificationEventRequest= NotificationEventRequest.builder()
                    .accountId(accountFollowing.getAccountId())
                    .message(message)
                    .type(NotificationType.FOLLOW.getName())
                    .build();
            String notificationJson = objectMapper.writeValueAsString(notificationEventRequest);
            kafkaTemplate.send(KafkaTopic.FOLLOW.getName(), String.valueOf(accountFollowing.getAccountId()),notificationJson);
        }catch (Exception e){
            throw new Exception(e.getMessage());
        }
    }
}
