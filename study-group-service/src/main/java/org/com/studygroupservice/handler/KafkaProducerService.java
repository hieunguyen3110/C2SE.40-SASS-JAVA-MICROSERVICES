package org.com.studygroupservice.handler;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.com.studygroupservice.dto.request.NotificationEventRequest;
import org.com.studygroupservice.dto.response.AccountDto;
import org.com.studygroupservice.dto.response.MessageEventDto;
import org.com.studygroupservice.dto.response.StudyGroupEventDto;
import org.com.studygroupservice.entity.GroupMember;
import org.com.studygroupservice.entity.JoinRequest;
import org.com.studygroupservice.enums.GroupMemberRole;
import org.com.studygroupservice.exception.ApiException;
import org.com.studygroupservice.repository.GroupMemberRepository;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaProducerService {
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final GroupMemberRepository groupMemberRepository;

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
