package org.com.studygroupservice.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.com.studygroupservice.dto.response.*;
import org.com.studygroupservice.entity.JoinRequest;
import org.com.studygroupservice.entity.Message;
import org.com.studygroupservice.entity.GroupMember;
import org.com.studygroupservice.entity.StudyGroup;
import org.com.studygroupservice.enums.GroupMemberRole;
import org.com.studygroupservice.exception.ApiException;
import org.com.studygroupservice.handler.KafkaProducerService;
import org.com.studygroupservice.repository.GroupMemberRepository;
import org.com.studygroupservice.repository.JoinRequestRepository;
import org.com.studygroupservice.repository.MessageRepository;
import org.com.studygroupservice.repository.StudyGroupRepository;
import org.com.studygroupservice.repository.httpClient.IdentityClient;
import org.com.studygroupservice.service.RedisService;
import org.com.studygroupservice.service.StudyGroupService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.*;
import java.util.stream.Collectors;

import static org.com.studygroupservice.constant.AppConstant.SUBJECT_KEY;
import static org.com.studygroupservice.constant.AppConstant.TTL_IN_SECONDS;

@Slf4j
@Service
@RequiredArgsConstructor
public class StudyGroupServiceImpl implements StudyGroupService {

    private final StudyGroupRepository groupRepository;
    private final GroupMemberRepository memberRepository;
    private final MessageRepository messageRepository;
    private final WebClient.Builder webClientBuilder;
    private final KafkaProducerService kafkaProducerService;
    private final JoinRequestRepository joinRequestRepository;
    private final RedisService redisService;
    private final ObjectMapper objectMapper;
    private final IdentityClient identityClient;
//    private final SimpMessagingTemplate simpMessagingTemplate;
    private final GroupMemberRepository groupMemberRepository;

    @Transactional
    @Override
    public StudyGroup createGroup(String groupName, String description, Long subjectId, boolean isPrivate, int memberLimited, List<Long> memberIds) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            AccountDto owner = (AccountDto) authentication.getPrincipal();

            SubjectDto subjectDto = fetchSubjectById(subjectId);
            if (subjectDto == null) {
                throw new ApiException(404, "Subject with ID " + subjectId + " not found.");
            }

            StudyGroup studyGroup = new StudyGroup();
            studyGroup.setName(groupName);
            studyGroup.setDescription(description);
            studyGroup.setSubjectId(subjectId);
            studyGroup.setPicture(null);
            studyGroup.setMemberLimited(memberLimited);
            studyGroup.setOwnerId(owner.getAccountId());
            studyGroup.setIsPrivate(isPrivate);
            studyGroup = groupRepository.save(studyGroup);

            final StudyGroup finalStudyGroup = studyGroup;

            Set<Long> uniqueMembers = new HashSet<>(memberIds);
            uniqueMembers.add(owner.getAccountId());

            List<GroupMember> groupMembers = uniqueMembers.stream()
                    .map(memberId -> {
                        GroupMemberRole role = memberId.equals(owner.getAccountId()) ? GroupMemberRole.OWNER : GroupMemberRole.MEMBER;
                        return new GroupMember(null, memberId, finalStudyGroup, role);
                    })
                    .collect(Collectors.toList());

            memberRepository.saveAll(groupMembers);

            StudyGroupEventDto event = new StudyGroupEventDto(
                    studyGroup.getId(),
                    owner.getAccountId(),
                    "Study group '" + groupName + "' has been created!",
                    isPrivate ? "Private" : "Public"
            );
            kafkaProducerService.sendStudyGroupEvent(event);

            return studyGroup;
        } catch (Exception e) {
            log.error("Failed to create group: {}", e.getMessage(), e);
            throw new ApiException(500, "Failed to create group: " + e.getMessage());
        }
    }

    @Override
    public List<SubjectDto> searchSubjectsByName(String subjectName) {
        try {
            List<SubjectDto> allSubjects = fetchSubjects();
            if (allSubjects == null || allSubjects.isEmpty()) {
                throw new ApiException(404, "No subjects available. Please try again later.");
            }

            return allSubjects.stream()
                    .filter(subject -> subject.getSubjectName().toLowerCase().contains(subjectName.toLowerCase()))
                    .collect(Collectors.toList());
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to fetch subjects: {}", e.getMessage(), e);
            throw new ApiException(500, "Failed to fetch subjects: " + e.getMessage());
        }
    }

    // Lấy SubjectDto theo subjectId với fallback
    private SubjectDto fetchSubjectById(Long subjectId) {
        List<SubjectDto> allSubjects = fetchSubjects();
        if (allSubjects == null || allSubjects.isEmpty()) {
            return null;
        }
        return allSubjects.stream()
                .filter(subject -> subject.getSubjectId().equals(subjectId))
                .findFirst()
                .orElse(null);
    }

    // Lấy danh sách SubjectDto từ Redis, fallback sang document-service nếu cần
    @Override
    public List<SubjectDto> fetchSubjects() {
        try {
            Object cachedValue = redisService.getData(SUBJECT_KEY);
            if (cachedValue != null) {
                return objectMapper.readValue(
                        cachedValue.toString(),
                        new TypeReference<List<SubjectDto>>() {}
                );
            }

            // Fallback: Gọi document-service nếu dữ liệu không có trong Redis
            log.warn("Subjects not found in Redis. Fetching from document-service via Eureka.");
            List<SubjectDto> subjects = webClientBuilder
                    .baseUrl("http://document-service")
                    .build()
                    .get()
                    .uri("/api/v1/document/subjects")
                    .retrieve()
                    .bodyToFlux(SubjectDto.class)
                    .collectList()
                    .block();

            if (subjects != null && !subjects.isEmpty()) {
                cacheSubjectsInRedis(subjects); // Lưu lại vào Redis với TTL
            }
            return subjects;
        } catch (Exception e) {
            log.error("Failed to fetch subjects from Redis or document-service: {}", e.getMessage(), e);
            return null;
        }
    }

    // Lưu danh sách SubjectDto vào Redis với TTL
    private void cacheSubjectsInRedis(List<SubjectDto> subjects) {
        try {
            String jsonSubjects = objectMapper.writeValueAsString(subjects);
            redisService.saveData(SUBJECT_KEY, jsonSubjects, TTL_IN_SECONDS);
            log.info("Cached {} subjects in Redis with TTL {} seconds (30 days)", subjects.size(), TTL_IN_SECONDS);
        } catch (Exception e) {
            log.error("Failed to cache subjects in Redis: {}", e.getMessage(), e);
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
    public void sendMessage(Long groupId, String content, Long senderId) {
        try {
            if (content == null || content.trim().isEmpty()) {
                throw new ApiException(400, "Nội dung tin nhắn không được để trống.");
            }

            StudyGroup group = groupRepository.findById(groupId)
                    .orElseThrow(() -> new ApiException(404, "Không tìm thấy nhóm học tập."));

            if (!isMember(groupId, senderId)) {
                throw new ApiException(403, "Người dùng không phải là thành viên của nhóm này.");
            }

            //Tạo và lưu tin nhắn
            Message message = new Message();
            message.setSenderId(senderId);
            message.setContent(content);
            message.setIsPinned(false);
            message.setGroup(group);

            Message savedMessage = messageRepository.save(message);

            MessageEventDto event = new MessageEventDto(
                    groupId, senderId, "send-message", savedMessage.getId(), content
            );
            kafkaProducerService.sendMessageEvent(event);

        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            log.error("Lỗi khi gửi tin nhắn: {}", e.getMessage(), e);
            throw new ApiException(500, "Không thể gửi tin nhắn. Vui lòng thử lại sau.");
        }
    }


    @Transactional
    @Override
    public void addMember(Long groupId, Long userId) {
        try {
            StudyGroup group = getGroupDetails(groupId);

            if (isMember(groupId, userId)) {
                throw new ApiException(400, "User is already a member of the group.");
            }

            GroupMember member = new GroupMember(null, userId, group);
            member.setRole(group.getOwnerId().equals(userId) ? GroupMemberRole.OWNER : GroupMemberRole.MEMBER);

            memberRepository.save(member);

            kafkaProducerService.sendMessageEvent(new MessageEventDto(groupId, userId, "add-member", null, null));
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
        StudyGroup group = getGroupDetails(groupId);

        if (isMember(groupId, userId)) {
            throw new ApiException(400, "User is already a member of the group.");
        }

        if (group.getIsPrivate()) {
            // Kiểm tra nếu đã có yêu cầu đang chờ
            if (joinRequestRepository.findByStudyGroupIdAndUserId(groupId, userId).isPresent()) {
                throw new ApiException(400, "You have already sent a request to join this private group.");
            }

            // Tạo yêu cầu tham gia nhóm
            JoinRequest joinRequest = new JoinRequest();
            joinRequest.setStudyGroup(group);
            joinRequest.setUserId(userId);
            joinRequest.setStatus(JoinRequest.RequestStatus.PENDING);
            joinRequestRepository.save(joinRequest);

            log.info("User {} requested to join private group {}", userId, groupId);
        } else {
            // Nếu là nhóm công khai, cho phép tham gia ngay lập tức
            GroupMember member = new GroupMember(null, userId, group);
            memberRepository.save(member);

            kafkaProducerService.sendMessageEvent(new MessageEventDto(groupId, userId, "join", null, null));
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

            kafkaProducerService.sendMessageEvent(new MessageEventDto(groupId, userId, "leave", null, null));
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

            message.setIsPinned(true);
            messageRepository.save(message);
            kafkaProducerService.sendMessageEvent(new MessageEventDto(group.getId(), currentUserId.getAccountId(), "pin_message", messageId, null));
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

            message.setIsPinned(false);
            messageRepository.save(message);

            kafkaProducerService.sendMessageEvent(new MessageEventDto(group.getId(), currentUserId.getAccountId(), "unpin_message", messageId, null));

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

            kafkaProducerService.sendMessageEvent(new MessageEventDto(groupId, currentUserId.getAccountId(), "delete", null, null));
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

            return membersPage.map(member -> {
                try {
                    AccountDto account = identityClient.getAccountId(member.getAccountId()).getData();

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
    public StudyGroup editGroup(Long groupId, String groupName, String description, Long subjectId, String picture, int memberLimited) {
        try {
            StudyGroup group = getGroupDetails(groupId);

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            AccountDto currentUser = (AccountDto) (authentication.getPrincipal());

            if (!group.getOwnerId().equals(currentUser.getAccountId())) {
                GroupMember membership = memberRepository.findByStudyGroupIdAndAccountId(groupId, currentUser.getAccountId())
                        .orElseThrow(() -> new ApiException(403, "User is not a member of this group"));
                if (!membership.getRole().equals(GroupMemberRole.ADMIN)) {
                    throw new ApiException(403, "Only owner or admin can edit group");
                }
            }

            if (groupName != null) {
                group.setName(groupName);
            }

            if (description != null) {
                group.setDescription(description);
            }

            if (subjectId != null) {
                group.setSubjectId(subjectId);
            }

            if (picture != null) {
                group.setPicture(picture);
            }

            if(memberLimited > 0) {
                group.setMemberLimited(memberLimited);
            }

            StudyGroup savedGroup = groupRepository.save(group);

            kafkaProducerService.sendMessageEvent(new MessageEventDto(groupId, currentUser.getAccountId(), "update-group", null, null));

            return savedGroup;
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to edit group: {}", e.getMessage(), e);
            throw new ApiException(500, "Failed to edit group: " + e.getMessage());
        }
    }

    @Transactional
    @Override
    public StudyGroup updatePrivacySetting(Long groupId, boolean isPrivate) {
        try {
            StudyGroup group = getGroupDetails(groupId);

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            AccountDto currentUser = (AccountDto) (authentication.getPrincipal());

            if (!group.getOwnerId().equals(currentUser.getAccountId())) {
                throw new ApiException(403, "Only owner can change privacy settings");
            }

            group.setIsPrivate(isPrivate);
            StudyGroup savedGroup = groupRepository.save(group);

            kafkaProducerService.sendMessageEvent(new MessageEventDto(
                    groupId,
                    currentUser.getAccountId(),
                    "privacy-update",
                    null,
                    "Group privacy setting changed to " + (isPrivate ? "Private" : "Public")
            ));

            return savedGroup;
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to update privacy setting: {}", e.getMessage(), e);
            throw new ApiException(500, "Failed to update privacy setting: " + e.getMessage());
        }
    }

    @Override
    public Page<Message> getPinnedMessages(Long groupId, Pageable pageable) {
        try {
            Page<Message> pinnedMessages = messageRepository.findByGroupIdAndIsPinnedTrue(groupId, pageable);
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
            message.setIsPinned(false);
            message.setDocumentLink(true);
            message.setDocumentId(documentId);
            message.setGroup(group);

            Message savedMessage = messageRepository.save(message);

            kafkaProducerService.sendMessageEvent(new MessageEventDto(groupId, senderId.getAccountId(), "share_document", savedMessage.getId(), null));

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

            kafkaProducerService.sendMessageEvent(new MessageEventDto(groupId, currentOwnerId.getAccountId(), "ownership_transfer", null, additionalData.toString()));
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

            kafkaProducerService.sendMessageEvent(new MessageEventDto(group.getId(), currentUserId.getAccountId(), "delete_message", messageId, null));

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

    @Override
    public List<StudyGroupEventDto> getGroupsByUserId() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            AccountDto currentUser = (AccountDto) authentication.getPrincipal();

            List<GroupMember> groupMembers = groupMemberRepository.findStudyGroupsByAccountId(currentUser.getAccountId());
            if (groupMembers.isEmpty()) {
                throw new ApiException(404, "User is not a member of any group");
            }



            return groupMembers.stream()
                    .map(groupMember -> {
                        StudyGroup group = groupMember.getStudyGroup();
                        StudyGroupEventDto dto = new StudyGroupEventDto();

                        Long subjectId = group.getSubjectId();

                        String subjectName = Objects.requireNonNull(fetchSubjectById(subjectId)).getSubjectName();
                        dto.setGroupId(group.getId());
                        dto.setGroupName(group.getName());
                        dto.setDescription(group.getDescription());
                        dto.setPicture(group.getPicture());
                        dto.setSubjectName(subjectName);
                        dto.setUserId(group.getOwnerId());
                        return dto;
                    })
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Failed to fetch groups by user ID: {}", e.getMessage(), e);
            throw new ApiException(500, "Failed to fetch groups by user ID: " + e.getMessage());
        }
    }



}