package org.com.studygroupservice.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.com.studygroupservice.entity.Message;
import org.com.studygroupservice.dto.response.AccountDto;
import org.com.studygroupservice.dto.response.GroupResponse;
import org.com.studygroupservice.entity.GroupMember;
import org.com.studygroupservice.entity.StudyGroup;
import org.com.studygroupservice.enums.GroupMemberRole;
import org.com.studygroupservice.exception.ApiException;
import org.com.studygroupservice.repository.GroupMemberRepository;
import org.com.studygroupservice.repository.MessageRepository;
import org.com.studygroupservice.repository.StudyGroupRepository;
import org.com.studygroupservice.service.StudyGroupService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class StudyGroupServiceImpl implements StudyGroupService {

    private final StudyGroupRepository groupRepository;
    private final GroupMemberRepository memberRepository;
    private final MessageRepository messageRepository;
    private final WebClient.Builder webClientBuilder;
    private final KafkaTemplate<String, Map<String, Object>> kafkaTemplate;

    @Transactional
    @Override
    public StudyGroup createGroup(String groupName) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            AccountDto ownerId = (AccountDto) authentication.getPrincipal();

            StudyGroup saveGroup = new StudyGroup();
            saveGroup.setName(groupName);
            saveGroup.setOwnerId(ownerId.getAccountId());
            saveGroup = groupRepository.save(saveGroup);

            GroupMember groupMember = new GroupMember();
            groupMember.setAccountId(ownerId.getAccountId());
            groupMember.setStudyGroup(saveGroup);
            groupMember.setRole(GroupMemberRole.OWNER);
            memberRepository.save(groupMember);

            sendNotificationToKafka(saveGroup.getId(), ownerId.getAccountId(), "created");

            return saveGroup;
        } catch (Exception e) {
            log.error("Failed to create group: {}", e.getMessage(), e);
            throw new ApiException(500, "Failed to create group: " + e.getMessage());
        }
    }

    @Override
    public StudyGroup getGroupDetails(Long groupId) {
        try {
            return groupRepository.findById(groupId)
                    .orElseThrow(() -> new ApiException(404, "Group not found"));
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to fetch group details: {}", e.getMessage(), e);
            throw new ApiException(500, "Failed to fetch group details: " + e.getMessage());
        }
    }

    @Transactional
    @Override
    public void sendMessage(Long groupId, String content) {
        try {
            if (content == null || content.trim().isEmpty()) {
                throw new ApiException(400, "Nội dung tin nhắn không được để trống.");
            }

            StudyGroup group = groupRepository.findById(groupId)
                    .orElseThrow(() -> new ApiException(404, "Nhóm học không tồn tại."));

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            AccountDto senderId = (AccountDto)(authentication.getPrincipal());

            if (!isMember(groupId, senderId.getAccountId())) {
                throw new ApiException(403, "Người dùng không phải là thành viên của nhóm.");
            }

            Message message = new Message();
            message.setSenderId(senderId.getAccountId());
            message.setContent(content);
            message.setPinned(false);
            message.setGroup(group);

            Message savedMessage = messageRepository.save(message);

            sendNotificationToKafka(groupId, senderId.getAccountId(), "send_message", savedMessage.getId(), content);
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            log.error("Lỗi khi gửi tin nhắn: {}", e.getMessage(), e);
            throw new ApiException(500, "Không thể gửi tin nhắn, vui lòng thử lại sau.");
        }
    }

    @Transactional
    @Override
    public void addMember(Long groupId, Long userId) {
        try {
            StudyGroup group = getGroupDetails(groupId);

            if (isMember(groupId, userId)) {
                throw new ApiException(400, "Người dùng đã là thành viên của nhóm.");
            }

            GroupMember member = new GroupMember(null, userId, group);

            if (group.getOwnerId().equals(userId)) {
                member.setRole(GroupMemberRole.OWNER);
            } else {
                member.setRole(GroupMemberRole.MEMBER);
            }

            memberRepository.save(member);

            sendNotificationToKafka(groupId, userId, "join");
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to add member: {}", e.getMessage(), e);
            throw new ApiException(500, "Failed to add member: " + e.getMessage());
        }
    }

    @Transactional
    @Override
    public void joinGroup(Long groupId, Long userId) {
        try {
            if (isMember(groupId, userId)) {
                throw new ApiException(400, "Người dùng đã là thành viên của nhóm.");
            }

            StudyGroup group = getGroupDetails(groupId);

            GroupMember member = new GroupMember(null, userId, group);
            memberRepository.save(member);

            sendNotificationToKafka(groupId, userId, "join");
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to join group: {}", e.getMessage(), e);
            throw new ApiException(500, "Failed to join group: " + e.getMessage());
        }
    }

    @Transactional
    @Override
    public void removeMember(Long groupId, Long userId) {
        try {
            StudyGroup group = getGroupDetails(groupId);

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            AccountDto currentUserId = (AccountDto) (authentication.getPrincipal());

            if (!group.getOwnerId().equals(currentUserId.getAccountId()) && !userId.equals(currentUserId.getAccountId())) {
                GroupMember membership = memberRepository.findByStudyGroupIdAndAccountId(groupId, currentUserId.getAccountId())
                        .orElseThrow(() -> new ApiException(403, "User is not a member of this group"));
                if (!membership.getRole().equals(GroupMemberRole.ADMIN)) {
                    throw new ApiException(403, "Only owner or admin can remove members, or user can leave group");
                }
            }

            GroupMember member = memberRepository.findByStudyGroupIdAndAccountId(groupId, userId)
                    .orElseThrow(() -> new ApiException(404, "Member not found"));

            memberRepository.delete(member);

            sendNotificationToKafka(groupId, userId, "leave");
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to remove member: {}", e.getMessage(), e);
            throw new ApiException(500, "Failed to remove member: " + e.getMessage());
        }
    }

    @Transactional
    @Override
    public void pinMessage(Long messageId) {
        try {
            Message message = messageRepository.findById(messageId)
                    .orElseThrow(() -> new ApiException(404, "Message not found"));

            StudyGroup group = message.getGroup();

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            AccountDto currentUserId = (AccountDto) (authentication.getPrincipal());

            if (!group.getOwnerId().equals(currentUserId.getAccountId())) {
                throw new ApiException(403, "Only owner can pin messages");
            }

            message.setPinned(true);
            messageRepository.save(message);

            sendNotificationToKafka(group.getId(), currentUserId.getAccountId(), "pin_message", messageId);
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to pin message: {}", e.getMessage(), e);
            throw new ApiException(500, "Failed to pin message: " + e.getMessage());
        }
    }

    @Transactional
    @Override
    public void unpinMessage(Long messageId) {
        try {
            Message message = messageRepository.findById(messageId)
                    .orElseThrow(() -> new ApiException(404, "Message not found"));

            StudyGroup group = message.getGroup();

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            AccountDto currentUserId = (AccountDto) (authentication.getPrincipal());

            if (!group.getOwnerId().equals(currentUserId.getAccountId())) {
                throw new ApiException(403, "Only owner can unpin messages");
            }

            message.setPinned(false);
            messageRepository.save(message);

            sendNotificationToKafka(group.getId(), currentUserId.getAccountId(), "unpin_message", messageId);
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to unpin message: {}", e.getMessage(), e);
            throw new ApiException(500, "Failed to unpin message: " + e.getMessage());
        }
    }

    @Transactional
    @Override
    public void deleteGroup(Long groupId) {
        try {
            StudyGroup group = groupRepository.findById(groupId)
                    .orElseThrow(() -> new ApiException(404, "Group not found"));

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            AccountDto currentUserId = (AccountDto) (authentication.getPrincipal());

            if (!group.getOwnerId().equals(currentUserId.getAccountId())) {
                GroupMember requesterMembership = memberRepository.findByStudyGroupIdAndAccountId(groupId, currentUserId.getAccountId())
                        .orElseThrow(() -> new ApiException(403, "User is not a member of this group"));
                if (!requesterMembership.getRole().equals(GroupMemberRole.OWNER)) {
                    throw new ApiException(403, "Only owner or admin can delete group");
                }
            }

            messageRepository.deleteByGroupId(groupId);
            memberRepository.deleteByStudyGroupId(groupId);
            groupRepository.delete(group);

            sendNotificationToKafka(groupId, currentUserId.getAccountId(), "delete");
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to delete group: {}", e.getMessage(), e);
            throw new ApiException(500, "Failed to delete group: " + e.getMessage());
        }
    }

    @Override
    public Page<GroupResponse> listMembers(Long groupId, Pageable pageable) {
        try {
            Page<GroupMember> membersPage = memberRepository.findByStudyGroupId(groupId, pageable);
            if (membersPage.isEmpty()) {
                throw new ApiException(404, "No members found in group");
            }

            WebClient webClient = webClientBuilder.baseUrl("http://identity-service").build();

            return membersPage.map(member -> {
                String url = "/admin/account/users/" + member.getAccountId();
                try {
                    AccountDto account = webClient.get()
                            .uri(url)
                            .retrieve()
                            .bodyToMono(AccountDto.class)
                            .block();

                    if (account == null) {
                        throw new ApiException(404, "Account not found for member ID: " + member.getAccountId());
                    }

                    return new GroupResponse(member.getId(), account.getUsername(), account.getEmail());
                } catch (Exception e) {
                    log.error("Error fetching account details: {}", e.getMessage(), e);
                    throw new ApiException(500, "Error fetching account details for member ID " + member.getAccountId());
                }
            });
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to list members: {}", e.getMessage(), e);
            throw new ApiException(500, "Failed to list members: " + e.getMessage());
        }
    }

    @Transactional
    @Override
    public StudyGroup editGroup(Long groupId, String groupName) {
        try {
            StudyGroup group = getGroupDetails(groupId);

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            AccountDto currentUserId = (AccountDto) (authentication.getPrincipal());

            if (!group.getOwnerId().equals(currentUserId.getAccountId())) {
                GroupMember membership = memberRepository.findByStudyGroupIdAndAccountId(groupId, currentUserId.getAccountId())
                        .orElseThrow(() -> new ApiException(403, "User is not a member of this group"));
                if (!membership.getRole().equals(GroupMemberRole.ADMIN)) {
                    throw new ApiException(403, "Only owner or admin can edit group");
                }
            }

            group.setName(groupName);
            StudyGroup savedGroup = groupRepository.save(group);

            sendNotificationToKafka(groupId, currentUserId.getAccountId(), "update_group");

            return savedGroup;
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to edit group: {}", e.getMessage(), e);
            throw new ApiException(500, "Failed to edit group: " + e.getMessage());
        }
    }

    @Override
    public Page<Message> getPinnedMessages(Long groupId, Pageable pageable) {
        try {
            Page<Message> pinnedMessages = messageRepository.findByGroupIdAndPinnedTrue(groupId, pageable);
            if (pinnedMessages.isEmpty()) {
                throw new ApiException(404, "No pinned messages found in group");
            }
            return pinnedMessages;
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to fetch pinned messages: {}", e.getMessage(), e);
            throw new ApiException(500, "Failed to fetch pinned messages: " + e.getMessage());
        }
    }

    @Override
    public Page<Message> getGroupMessages(Long groupId, Pageable pageable) {
        try {
            Page<Message> messages = messageRepository.findByGroupId(groupId, pageable);
            if (messages.isEmpty()) {
                throw new ApiException(404, "No messages found in group");
            }
            return messages;
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to fetch group messages: {}", e.getMessage(), e);
            throw new ApiException(500, "Failed to fetch group messages: " + e.getMessage());
        }
    }

    @Transactional
    @Override
    public Message shareDocumentToGroup(Long groupId, String documentId, String shareUrl) {
        try {
            StudyGroup group = groupRepository.findById(groupId)
                    .orElseThrow(() -> new ApiException(404, "Nhóm học không tồn tại."));

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            AccountDto senderId = (AccountDto) (authentication.getPrincipal());

            if (!isMember(groupId, senderId.getAccountId())) {
                throw new ApiException(403, "Người dùng không phải là thành viên của nhóm.");
            }

            Message message = new Message();
            message.setSenderId(senderId.getAccountId());
            message.setContent("Đã chia sẻ tài liệu: " + shareUrl);
            message.setPinned(false);
            message.setDocumentLink(true);
            message.setDocumentId(documentId);
            message.setGroup(group);

            Message savedMessage = messageRepository.save(message);

            sendNotificationToKafka(groupId, senderId.getAccountId(), "share_document", savedMessage.getId());

            return savedMessage;
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            log.error("Lỗi khi chia sẻ tài liệu vào nhóm: {}", e.getMessage(), e);
            throw new ApiException(500, "Không thể chia sẻ tài liệu: " + e.getMessage());
        }
    }

    private boolean isMember(Long groupId, Long userId) {
        try {
            return memberRepository.findByStudyGroupIdAndAccountId(groupId, userId).isPresent();
        } catch (Exception e) {
            log.error("Error checking membership: {}", e.getMessage(), e);
            return false;
        }
    }

    private void sendNotificationToKafka(Long groupId, Long userId, String action, Long messageId, Object additionalData) {
        try {
            Map<String, Object> notification = new HashMap<>();
            notification.put("groupId", groupId);
            notification.put("userId", userId);
            notification.put("action", action);
            if(messageId != null){
                notification.put("messageId", messageId);
            }
            if(additionalData != null){
                notification.put("data", additionalData);
            }
            notification.put("timestamp", System.currentTimeMillis());

            kafkaTemplate.send("study-group-topic", String.valueOf(userId), notification);
            log.info("Notification sent to Kafka: {}", notification);
        } catch (Exception e) {
            log.error("Failed to send notification to Kafka: {}", e.getMessage(), e);
        }
    }

    private void sendNotificationToKafka(Long groupId, Long userId, String action) {
        sendNotificationToKafka(groupId, userId, action, null, null);
    }

    private void sendNotificationToKafka(Long groupId, Long userId, String action, Long messageId) {
        sendNotificationToKafka(groupId, userId, action, messageId, null);
    }

    @Override
    public List<StudyGroup> findUserGroups(Long userId) {
        try {
            List<GroupMember> memberships = memberRepository.findByAccountId(userId);
            if (memberships.isEmpty()) {
                throw new ApiException(404, "User is not a member of any group");
            }

            return memberships.stream()
                    .map(GroupMember::getStudyGroup)
                    .collect(Collectors.toList());
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to find user groups: {}", e.getMessage(), e);
            throw new ApiException(500, "Failed to find user groups: " + e.getMessage());
        }
    }

    @Transactional
    @Override
    public void transferOwnership(Long groupId, Long newOwnerId) {
        try {
            StudyGroup group = getGroupDetails(groupId);

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            AccountDto currentOwnerId = (AccountDto) (authentication.getPrincipal());

            if (!group.getOwnerId().equals(currentOwnerId.getAccountId())) {
                throw new ApiException(403, "Only current owner can transfer ownership");
            }

            if (!isMember(groupId, newOwnerId)) {
                throw new ApiException(404, "New owner must be a member of the group");
            }

            group.setOwnerId(newOwnerId);
            groupRepository.save(group);

            Map<String, Object> additionalData = Map.of(
                    "newOwnerId", newOwnerId,
                    "previousOwnerId", currentOwnerId.getAccountId()
            );
            sendNotificationToKafka(groupId, currentOwnerId.getAccountId(), "ownership_transfer", null, additionalData);
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to transfer ownership: {}", e.getMessage(), e);
            throw new ApiException(500, "Failed to transfer ownership: " + e.getMessage());
        }
    }

    @Transactional
    @Override
    public void deleteMessage(Long messageId) {
        try {
            Message message = messageRepository.findById(messageId)
                    .orElseThrow(() -> new ApiException(404, "Message not found"));

            StudyGroup group = message.getGroup();

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            AccountDto currentUserId = (AccountDto) (authentication.getPrincipal());

            if (!message.getSenderId().equals(currentUserId.getAccountId()) && !group.getOwnerId().equals(currentUserId.getAccountId())) {
                throw new ApiException(403, "Only message sender or group owner can delete messages");
            }

            messageRepository.delete(message);

            sendNotificationToKafka(group.getId(), currentUserId.getAccountId(), "delete_message", messageId);
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to delete message: {}", e.getMessage(), e);
            throw new ApiException(500, "Failed to delete message: " + e.getMessage());
        }
    }

    @Override
    public List<StudyGroup> searchGroups(String keyword) {
        try {
            List<StudyGroup> groups = groupRepository.findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
                    keyword, keyword);

            if (groups.isEmpty()) {
                throw new ApiException(404, "No matching groups found");
            }

            return groups;
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to search groups: {}", e.getMessage(), e);
            throw new ApiException(500, "Failed to search groups: " + e.getMessage());
        }
    }

    @Override
    public boolean isGroupOwner(Long groupId, Long userId) {
        try {
            StudyGroup group = getGroupDetails(groupId);
            return group.getOwnerId().equals(userId);
        } catch (Exception e) {
            return false;
        }
    }
}