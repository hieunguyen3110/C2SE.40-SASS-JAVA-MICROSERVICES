package org.com.studygroupservice.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.com.studygroupservice.entity.Message;
import org.com.studygroupservice.dto.response.AccountDto;
import org.com.studygroupservice.dto.response.GroupResponse;
import org.com.studygroupservice.entity.GroupMember;
import org.com.studygroupservice.entity.StudyGroup;
import org.com.studygroupservice.exception.ApiException;
import org.com.studygroupservice.repository.GroupMemberRepository;
import org.com.studygroupservice.repository.MessageRepository;
import org.com.studygroupservice.repository.StudyGroupRepository;
import org.com.studygroupservice.service.StudyGroupService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

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
    private final SimpMessagingTemplate messagingTemplate;

    @Transactional
    @Override
    public StudyGroup createGroup(StudyGroup group, Long ownerId) {
        try {
            group.setOwnerId(ownerId);
            group = groupRepository.save(group);
            addMember(group.getId(), ownerId);
            return group;
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
    public void sendMessage(Long groupId, Long senderId, String content) {
        try {
            if (content == null || content.trim().isEmpty()) {
                throw new ApiException(400, "Nội dung tin nhắn không được để trống.");
            }

            StudyGroup group = groupRepository.findById(groupId)
                    .orElseThrow(() -> new ApiException(404, "Nhóm học không tồn tại."));

            // Kiểm tra xem người gửi có phải là thành viên của nhóm không
            if (!isMember(groupId, senderId)) {
                throw new ApiException(403, "Người dùng không phải là thành viên của nhóm.");
            }

            Message message = new Message();
            message.setSenderId(senderId);
            message.setContent(content);
            message.setPinned(false);
            message.setGroup(group);

            Message savedMessage = messageRepository.save(message);

            try {
                messagingTemplate.convertAndSend("/topic/group-" + groupId, savedMessage);
                log.info("Tin nhắn được gửi đến WebSocket: {}", savedMessage);
            } catch (Exception e) {
                log.error("Lỗi khi gửi tin nhắn qua WebSocket: {}", e.getMessage(), e);
                throw new ApiException(500, "Lỗi khi gửi tin nhắn qua WebSocket.");
            }

        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            log.error("Lỗi khi lưu tin nhắn vào database: {}", e.getMessage(), e);
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
            memberRepository.save(member);

            notifyGroupMemberChange(groupId, userId, "join");
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

            // Kiểm tra xem nhóm có tồn tại không
            StudyGroup group = getGroupDetails(groupId);

            GroupMember member = new GroupMember(null, userId, group);
            memberRepository.save(member);

            // Thông báo cho nhóm về thành viên mới
            notifyGroupMemberChange(groupId, userId, "join");
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to join group: {}", e.getMessage(), e);
            throw new ApiException(500, "Failed to join group: " + e.getMessage());
        }
    }

    @Transactional
    @Override
    public void removeMember(Long groupId, Long userId, Long requesterId) {
        try {
            StudyGroup group = getGroupDetails(groupId);
            if (!group.getOwnerId().equals(requesterId) && !userId.equals(requesterId)) {
                throw new ApiException(403, "Only owner can remove members or user can leave group");
            }

            GroupMember member = memberRepository.findByStudyGroupIdAndAccountId(groupId, userId)
                    .orElseThrow(() -> new ApiException(404, "Member not found"));

            memberRepository.delete(member);

            notifyGroupMemberChange(groupId, userId, "leave");
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to remove member: {}", e.getMessage(), e);
            throw new ApiException(500, "Failed to remove member: " + e.getMessage());
        }
    }

    @Transactional
    @Override
    public void pinMessage(Long messageId, Long requesterId) {
        try {
            Message message = messageRepository.findById(messageId)
                    .orElseThrow(() -> new ApiException(404, "Message not found"));
            StudyGroup group = message.getGroup();
            if (!group.getOwnerId().equals(requesterId)) {
                throw new ApiException(403, "Only owner can pin messages");
            }
            message.setPinned(true);
            messageRepository.save(message);

            messagingTemplate.convertAndSend("/topic/group-" + group.getId() + "/pin", message);
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to pin message: {}", e.getMessage(), e);
            throw new ApiException(500, "Failed to pin message: " + e.getMessage());
        }
    }

    @Transactional
    @Override
    public void unpinMessage(Long messageId, Long requesterId) {
        try {
            Message message = messageRepository.findById(messageId)
                    .orElseThrow(() -> new ApiException(404, "Message not found"));
            StudyGroup group = message.getGroup();
            if (!group.getOwnerId().equals(requesterId)) {
                throw new ApiException(403, "Only owner can unpin messages");
            }
            message.setPinned(false);
            messageRepository.save(message);

            messagingTemplate.convertAndSend("/topic/group-" + group.getId() + "/unpin", message);
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to unpin message: {}", e.getMessage(), e);
            throw new ApiException(500, "Failed to unpin message: " + e.getMessage());
        }
    }

    @Transactional
    @Override
    public void deleteGroup(Long groupId, Long requesterId) {
        try {
            StudyGroup group = getGroupDetails(groupId);
            if (!group.getOwnerId().equals(requesterId)) {
                throw new ApiException(403, "Only owner can delete group");
            }

            // Xóa tất cả tin nhắn của nhóm trước
            messageRepository.deleteByGroupId(groupId);

            // Xóa tất cả thành viên của nhóm
            memberRepository.deleteByStudyGroupId(groupId);

            // Xóa nhóm
            groupRepository.delete(group);

            // Thông báo cho tất cả thành viên về việc nhóm đã bị xóa
            messagingTemplate.convertAndSend("/topic/group-" + groupId + "/delete",
                    Map.of("message", "Group has been deleted", "groupId", groupId));
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
                    throw new ApiException(500, "Error fetching account details for member ID " + member.getAccountId() + ": " + e.getMessage());
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
    public StudyGroup editGroup(Long groupId, StudyGroup updatedGroup, Long requesterId) {
        try {
            StudyGroup group = getGroupDetails(groupId);
            if (!group.getOwnerId().equals(requesterId)) {
                throw new ApiException(403, "Only owner can edit group");
            }

            group.setName(updatedGroup.getName());
            group.setDescription(updatedGroup.getDescription());

            StudyGroup savedGroup = groupRepository.save(group);

            messagingTemplate.convertAndSend("/topic/group-" + groupId + "/update", savedGroup);

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

    @Override
    public Message shareDocumentToGroup(Long groupId, Long senderId, String documentId, String shareUrl) throws Exception {
        try {
            // Kiểm tra nhóm có tồn tại không
            StudyGroup group = groupRepository.findById(groupId)
                    .orElseThrow(() -> new Exception("Nhóm học không tồn tại."));

            // Kiểm tra senderId có phải thành viên nhóm không (giả định có phương thức isMember)
            if (!isMember(groupId, senderId)) {
                throw new Exception("Người dùng không phải là thành viên của nhóm.");
            }

            // Tạo tin nhắn chia sẻ
            Message message = new Message();
            message.setSenderId(senderId);
            message.setContent("Đã chia sẻ tài liệu: " + shareUrl);
            message.setPinned(false);
            message.setDocumentLink(true);
            message.setDocumentId(documentId);
            message.setGroup(group);

            Message savedMessage = messageRepository.save(message);

            // Gửi tin nhắn qua WebSocket
            messagingTemplate.convertAndSend("/topic/group-" + groupId, savedMessage);
            log.info("Tin nhắn chia sẻ tài liệu được gửi đến WebSocket: {}", savedMessage);

            return savedMessage;
        } catch (Exception e) {
            log.error("Lỗi khi chia sẻ tài liệu vào nhóm: {}", e.getMessage());
            throw new Exception("Không thể chia sẻ tài liệu: " + e.getMessage());
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

    private void notifyGroupMemberChange(Long groupId, Long userId, String action) {
        try {
            WebClient webClient = webClientBuilder.baseUrl("http://identity-service").build();
            AccountDto account = webClient.get()
                    .uri("/admin/account/users/" + userId)
                    .retrieve()
                    .bodyToMono(AccountDto.class)
                    .block();

            if (account != null) {
                Map<String, Object> notification = Map.of(
                        "userId", userId,
                        "username", account.getUsername(),
                        "action", action
                );

                messagingTemplate.convertAndSend("/topic/group-" + groupId + "/member", notification);
            }
        } catch (Exception e) {
            log.error("Failed to notify group about member change: {}", e.getMessage(), e);
        }
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

    @Override
    public void transferOwnership(Long groupId, Long newOwnerId, Long currentOwnerId) {
        try {
            StudyGroup group = getGroupDetails(groupId);

            if (!group.getOwnerId().equals(currentOwnerId)) {
                throw new ApiException(403, "Only current owner can transfer ownership");
            }

            if (!isMember(groupId, newOwnerId)) {
                throw new ApiException(404, "New owner must be a member of the group");
            }

            group.setOwnerId(newOwnerId);
            StudyGroup updatedGroup = groupRepository.save(group);

            Map<String, Object> notification = Map.of(
                    "action", "ownership_transfer",
                    "groupId", groupId,
                    "newOwnerId", newOwnerId,
                    "previousOwnerId", currentOwnerId
            );

            messagingTemplate.convertAndSend("/topic/group-" + groupId + "/ownership", notification);
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to transfer ownership: {}", e.getMessage(), e);
            throw new ApiException(500, "Failed to transfer ownership: " + e.getMessage());
        }
    }

    @Override
    public void deleteMessage(Long messageId, Long requesterId) {
        try {
            Message message = messageRepository.findById(messageId)
                    .orElseThrow(() -> new ApiException(404, "Message not found"));

            StudyGroup group = message.getGroup();

            if (!message.getSenderId().equals(requesterId) && !group.getOwnerId().equals(requesterId)) {
                throw new ApiException(403, "Only message sender or group owner can delete messages");
            }

            messageRepository.delete(message);

            messagingTemplate.convertAndSend("/topic/group-" + group.getId() + "/delete-message",
                    Map.of("messageId", messageId));
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