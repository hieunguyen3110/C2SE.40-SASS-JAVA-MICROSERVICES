package org.com.studygroupservice.handler;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.com.studygroupservice.dto.request.NotificationEventRequest;
import org.com.studygroupservice.dto.response.*;
import org.com.studygroupservice.entity.GroupMember;
import org.com.studygroupservice.entity.JoinRequest;
import org.com.studygroupservice.entity.Message;
import org.com.studygroupservice.entity.StudyGroup;
import org.com.studygroupservice.enums.GroupMemberRole;
import org.com.studygroupservice.enums.MessageType;
import org.com.studygroupservice.exception.ApiException;
import org.com.studygroupservice.repository.GroupMemberRepository;
import org.com.studygroupservice.repository.MessageRepository;
import org.com.studygroupservice.repository.httpClient.IdentityClient;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaProducerService {
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final GroupMemberRepository groupMemberRepository;
    private final MessageRepository messageRepository;
    private final IdentityClient identityClient;

//    public void sendStudyGroupEvent(StudyGroupEventDto event) {
//        try {
//            String message = objectMapper.writeValueAsString(event);
//            kafkaTemplate.send("study-group-ws-topic", event.getUserId().toString(), message);
//            log.info("Sent study group event to Kafka: {}", message);
//        } catch (JsonProcessingException e) {
//            log.error("Error converting event to JSON: {}", e.getMessage());
//        }
//    }

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

    public void sendNotificationRejectOrRemoveGroup(Long accountId, String type, String message){
        try{
            NotificationEventRequest request = NotificationEventRequest.builder()
                    .accountId(accountId)
                    .type(type)
                    .message(message)
                    .build();
            String jsonNotification = objectMapper.writeValueAsString(request);
            kafkaTemplate.send("save-notification-topic",accountId.toString(),jsonNotification);
        }catch (JsonProcessingException e){
            log.error("Error converting event to JSON: {}", e.getMessage());
        }
    }

    public void sendMessageEvent(MessageEventDto event, StudyGroup studyGroup, Boolean isJoinGroup) {
        try {
            AccountDto getAccount = identityClient.getAccountId(event.getSenderId()).getData();
            String message;
            String username = null;
            if(getAccount.getFirstName() == null || getAccount.getLastName() == null){
                message =  getAccount.getEmail() + " has joined the group.";
            }else{
                username= getAccount.getFirstName() + " " + getAccount.getLastName();
                message = username + " has joined the group.";
            }
            Message messageSave = Message.builder()
                    .senderId(0L)
                    .isPinned(false)
                    .documentLink(false)
                    .group(studyGroup)
                    .content(message)
                    .documentId(null)
                    .messageType(MessageType.NOTIFICATION)
                    .documentName(null)
                    .docFilePath(null)
                    .documentId(null)
                    .build();
            Message messageResult = messageRepository.save(messageSave);
            MessageResponse messageResponse = MessageResponse.builder()
                    .senderId(0L)
                    .groupId(event.getGroupId())
                    .content(message)
                    .messageId(messageResult.getId())
                    .timestamp(LocalDateTime.now().toString())
                    .username(username!=null? username : "")
                    .profilePicture(getAccount.getProfilePicture())
                    .messageType(messageResult.getMessageType().getMessageType())
                    .documentName(messageResult.getDocumentName())
                    .docFilePath(messageResult.getDocFilePath())
                    .documentId(messageResult.getDocumentId())
                    .build();
            String jsonMessage = objectMapper.writeValueAsString(messageResponse);
            NotificationEventRequest request = NotificationEventRequest.builder()
                    .accountId(getAccount.getAccountId())
                    .type(event.getEventType())
                    .message("You have been approved to join the group " + studyGroup.getName())
                    .build();
            String jsonNotification = objectMapper.writeValueAsString(request);
            if(isJoinGroup){
                kafkaTemplate.send("send-message-ws-topic",event.getGroupId().toString(),jsonMessage);
            }else{
                kafkaTemplate.send("save-notification-topic",getAccount.getAccountId().toString(),jsonNotification);
                kafkaTemplate.send("send-message-ws-topic",event.getGroupId().toString(),jsonMessage);
            }
            log.info("Sent message event to Kafka: {}", jsonMessage);
            log.info("Sent notification event to Kafka: {}", jsonNotification);
        } catch (JsonProcessingException e) {
            log.error("Error converting event to JSON: {}", e.getMessage());
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

    public void sendRoleUpdateNotification(Long groupId, Long userId, GroupMemberRole role, String groupName, AccountDto user) {
        try {
            String userMessage = String.format("Vai trò của bạn trong nhóm %s đã được cập nhật thành %s.", groupName, role);
            NotificationEventRequest userNotification = new NotificationEventRequest(userId, userMessage, "ROLE_UPDATED");
            String userJson = objectMapper.writeValueAsString(userNotification);
            kafkaTemplate.send("save-notification-topic", userId.toString(), userJson);
            log.info("Sent role update notification to user {} for group {}", userId, groupId);

        } catch (JsonProcessingException e) {
            log.error("Failed to serialize notification for user {} in group {}: {}", userId, groupId, e.getMessage(), e);
            throw new ApiException(500, "Failed to send notification");
        }
    }
}
