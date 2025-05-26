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
import org.com.studygroupservice.enums.ErrorCode;
import org.com.studygroupservice.enums.GroupMemberRole;
import org.com.studygroupservice.exception.ApiException;
import org.com.studygroupservice.handler.KafkaProducerService;
import org.com.studygroupservice.repository.GroupMemberRepository;
import org.com.studygroupservice.repository.JoinRequestRepository;
import org.com.studygroupservice.repository.MessageRepository;
import org.com.studygroupservice.repository.StudyGroupRepository;
import org.com.studygroupservice.repository.httpClient.DocumentClient;
import org.com.studygroupservice.repository.httpClient.IdentityClient;
import org.com.studygroupservice.service.FirebaseService;
import org.com.studygroupservice.service.RedisService;
import org.com.studygroupservice.service.StudyGroupService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
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
    private final KafkaProducerService kafkaProducerService;
    private final JoinRequestRepository joinRequestRepository;
    private final RedisService redisService;
    private final ObjectMapper objectMapper;
    private final IdentityClient identityClient;
    private final DocumentClient documentClient;
    private final GroupMemberRepository groupMemberRepository;
    private final FirebaseService firebaseService;

    @Transactional
    @Override
    public StudyGroup createGroup(String groupName, String description, Long subjectId, Boolean isPrivate, Integer memberLimited, List<Long> memberIds) {
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

            groupRepository.save(studyGroup);

            Set<Long> uniqueMembers = new HashSet<>(memberIds);
            uniqueMembers.add(owner.getAccountId());

            List<GroupMember> groupMembers = new ArrayList<>();
            for (Long memberId : uniqueMembers) {
                GroupMemberRole role = memberId.equals(owner.getAccountId()) ? GroupMemberRole.OWNER : GroupMemberRole.MEMBER;
                groupMembers.add(new GroupMember(null, memberId, studyGroup, role));
            }

            memberRepository.saveAll(groupMembers);

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
                return Collections.emptyList();
            }

            return allSubjects.stream()
                    .filter(subject -> subject.getSubjectName().toLowerCase().contains(subjectName.toLowerCase()))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Failed to fetch subjects: {}", e.getMessage(), e);
            throw new ApiException(500, "Failed to fetch subjects: " + e.getMessage());
        }
    }

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

    @Override
    public List<SubjectDto> fetchSubjects() {
        try {
            String json = (String) redisService.getData(SUBJECT_KEY);
            if (json != null) {
                return objectMapper.readValue(
                        json,
                        new TypeReference<>() {}
                );
            }

            log.warn("Subjects not found in Redis. Fetching from document-service via Eureka.");
            List<SubjectDto> subjects = documentClient.getAllSubject().getData();

            if (subjects == null || subjects.isEmpty()) {
                return Collections.emptyList();
            }

            cacheSubjectsInRedis(subjects);
            return subjects;
        } catch (Exception e) {
            log.error("Failed to fetch subjects from Redis or document-service: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

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
    public StudyGroupEventDto getGroupDetails(Long groupId) {
        try {
            StudyGroup group = groupRepository.findById(groupId)
                    .orElseThrow(() -> new ApiException(404, "Group with ID " + groupId + " not found."));

            SubjectDto subject = fetchSubjectById(group.getSubjectId());
            if (subject == null) {
                throw new ApiException(404, "Subject with ID " + group.getSubjectId() + " not found.");
            }

            List<JoinRequest> joinRequests = joinRequestRepository.findByStudyGroupIdAndStatus(groupId, JoinRequest.RequestStatus.PENDING);
            List<JoinRequestDto> joinRequestDtos = Collections.emptyList();

            if (!joinRequests.isEmpty()) {
                Set<Long> userIds = joinRequests.stream()
                        .map(JoinRequest::getAccountId)
                        .collect(Collectors.toSet());

                Map<Long, AccountDto> userDetailsMap = getAccountsWithCache(userIds);

                joinRequestDtos = joinRequests.stream()
                        .map(joinRequest -> {
                            JoinRequestDto dto = new JoinRequestDto();
                            dto.setId(joinRequest.getId());
                            dto.setUserId(joinRequest.getAccountId());
                            dto.setStatus(joinRequest.getStatus().name());
                            dto.setCreatedAt(joinRequest.getCreatedAt());

                            AccountDto account = userDetailsMap.get(joinRequest.getAccountId());
                            if (account == null) {
                                log.warn("Account not found for user ID: {}", joinRequest.getAccountId());
                                dto.setName("Unknown");
                                dto.setAvatar(null);
                                dto.setEmail("Unknown");
                            } else {
                                dto.setName(account.getFirstName() + " " + account.getLastName());
                                dto.setAvatar(account.getProfilePicture());
                                dto.setEmail(account.getEmail());
                            }
                            return dto;
                        })
                        .collect(Collectors.toList());
            }

            return new StudyGroupEventDto(
                    group.getId(),
                    group.getOwnerId(),
                    group.getIsPrivate(),
                    group.getName(),
                    group.getDescription(),
                    subject.getSubjectName(),
                    group.getPicture(),
                    group.getMemberLimited(),
                    joinRequestDtos,
                    groupRepository.getMemberCount(group.getId()),
                    group.getCreatedAt()
            );
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to fetch group details: {}", e.getMessage(), e);
            throw new ApiException(500, "Failed to fetch group details: " + e.getMessage());
        }
    }

    private Map<Long, AccountDto> getAccountsWithCache(Set<Long> userIds) {
        Map<Long, AccountDto> result = new HashMap<>();

        List<Long> missingUserIds = new ArrayList<>();
        for (Long userId : userIds) {
            String cacheKey = "account:" + userId;
            Object cachedAccount = redisService.getData(cacheKey);
            if (cachedAccount != null) {
                try {
                    AccountDto account = objectMapper.readValue(cachedAccount.toString(), AccountDto.class);
                    result.put(userId, account);
                } catch (Exception e) {
                    log.error("Failed to deserialize account from Redis for user ID {}: {}", userId, e.getMessage());
                    missingUserIds.add(userId);
                }
            } else {
                missingUserIds.add(userId);
            }
        }

        if (!missingUserIds.isEmpty()) {
            try {
                ApiResponse<List<AccountDto>> response = identityClient.getAccountsByIds(new HashSet<>(missingUserIds));
                List<AccountDto> accounts = response.getData();
                if (!accounts.isEmpty()) {
                    for (AccountDto account : accounts) {
                        if (account != null && account.getAccountId() != null) {
                            result.put(account.getAccountId(), account);
                            String cacheKey = "account:" + account.getAccountId();
                            try {
                                String jsonAccount = objectMapper.writeValueAsString(account);
                                redisService.saveData(cacheKey, jsonAccount, TTL_IN_SECONDS);
                            } catch (Exception e) {
                                log.error("Failed to cache account in Redis for user ID {}: {}", account.getAccountId(), e.getMessage());
                            }
                        }
                    }
                }
            } catch (Exception e) {
                log.error("Failed to fetch accounts from IdentityClient: {}", e.getMessage(), e);
            }
        }
        return result;
    }

    @Transactional
    @Override
    public void sendMessage(Long groupId, String content, Long senderId) {
        try {
            if (content == null || content.trim().isEmpty()) {
                throw new ApiException(400, "Message content cannot be empty.");
            }

            StudyGroup group = groupRepository.findById(groupId)
                    .orElseThrow(() -> new ApiException(404, "Group not found."));

            if (!isMember(groupId, senderId)) {
                throw new ApiException(403, "User is not a member of this group.");
            }

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
            log.error("Failed to send message: {}", e.getMessage(), e);
            throw new ApiException(500, "Failed to send message: " + e.getMessage());
        }
    }

    @Transactional
    @Override
    public void addMember(Long groupId, Long userId) {
        try {
            StudyGroup group = groupRepository.findById(groupId)
                    .orElseThrow(() -> new ApiException(404, "Group not found."));

            if (isMember(groupId, userId)) {
                throw new ApiException(400, "User is already a member of the group.");
            }

            GroupMember member = new GroupMember(null, userId, group);
            member.setRole(group.getOwnerId().equals(userId) ? GroupMemberRole.OWNER : GroupMemberRole.MEMBER);

            memberRepository.save(member);

            kafkaProducerService.sendMessageEvent(new MessageEventDto(groupId, userId, "add-member", null, null));
        } catch (Exception e) {
            log.error("Failed to add member: {}", e.getMessage(), e);
            throw new ApiException(500, "Failed to add member: " + e.getMessage());
        }
    }
    @Transactional
    @Override
    public StudyGroupEventDto joinGroup(Long groupId, AccountDto accountDto) throws Exception {
        try{
            StudyGroup group =  groupRepository.findById(groupId)
                    .orElseThrow(()->new ApiException(ErrorCode.BAD_REQUEST.getStatusCode().value(),"Group not found"));

            if (isMember(groupId, accountDto.getAccountId())) {
                throw new ApiException(400, "User is already a member of the group.");
            }
            if(group.getMemberLimited()==group.getMembers().size()){
                throw new ApiException(ErrorCode.BAD_REQUEST.getStatusCode().value(),"The group enough members");
            }

            if (group.getIsPrivate()) {
                // Kiểm tra nếu đã có yêu cầu đang chờ
                if (joinRequestRepository.findByStudyGroupIdAndAccountId(groupId, accountDto.getAccountId()).isPresent()) {
                    throw new ApiException(400, "You have already sent a request to join this private group.");
                }
                // Tạo yêu cầu tham gia nhóm
                JoinRequest joinRequest = new JoinRequest();
                joinRequest.setStudyGroup(group);
                joinRequest.setAccountId(accountDto.getAccountId());
                joinRequest.setStatus(JoinRequest.RequestStatus.PENDING);
                joinRequestRepository.save(joinRequest);
                List<Long> filterAdminMember= group.getMembers().stream()
                        .filter(member-> member.getRole().equals(GroupMemberRole.ADMIN))
                        .map(GroupMember::getAccountId)
                        .toList();
                kafkaProducerService.sendJoinRequestEvent(accountDto,group.getOwnerId(),filterAdminMember,group.getName());
                log.info("User {} requested to join private group {}", accountDto.getAccountId(), groupId);
                return null;
            } else {
                // Nếu là nhóm công khai, cho phép tham gia ngay lập tức
                GroupMember member = new GroupMember(null, accountDto.getAccountId(), group);
                memberRepository.save(member);
                MessageEventDto messageEventDto = new MessageEventDto(
                        groupId,
                        accountDto.getAccountId(),
                        "JOIN_APPROVE",
                        null,
                        "User " + accountDto.getAccountId() + " has been to join the group " + group.getName()
                );

                kafkaProducerService.sendMessageEvent(messageEventDto, group, true);
                return StudyGroupEventDto.builder()
                        .groupName(group.getName())
                        .groupId(groupId)
                        .description(group.getDescription())
                        .createdAt(group.getCreatedAt())
                        .memberCount(group.getMembers().size())
                        .picture(group.getPicture())
                        .memberLimited(group.getMemberLimited())
                        .isPrivate(group.getIsPrivate())
                        .build();
            }
        }catch (Exception e){
            throw new Exception(e);
        }
    }

    @Transactional
    @Override
    public void approveJoinRequest(Long joinRequestId) {
        try {
            // Tìm JoinRequest với thông tin nhóm được tải cùng lúc
            JoinRequest joinRequest = joinRequestRepository.findById(joinRequestId)
                    .orElseThrow(() -> new ApiException(404, "Join request with ID " + joinRequestId + " not found."));

            if (joinRequest.getStatus() != JoinRequest.RequestStatus.PENDING) {
                throw new ApiException(400, "Join request has already been processed (status: " + joinRequest.getStatus() + ").");
            }

            StudyGroup group = joinRequest.getStudyGroup();
            if (group == null) {
                throw new ApiException(404, "Associated group not found for join request ID " + joinRequestId + ".");
            }

            Long groupId = group.getId();
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            AccountDto currentUser = (AccountDto) authentication.getPrincipal();
            List<GroupMember> groupMembers = groupMemberRepository.findByStudyGroupId(groupId);
            List<Long> adminIds = groupMembers.stream()
                    .filter(groupMember -> groupMember.getRole().equals(GroupMemberRole.ADMIN))
                    .map(GroupMember::getAccountId)
                    .toList();
            if(!group.getOwnerId().equals(currentUser.getAccountId())){
                if(!adminIds.contains(currentUser.getAccountId())){
                    throw new ApiException(403, "Only owner or admin can approve join requests.");
                }
            }

            Long accountId = joinRequest.getAccountId();
            if (memberRepository.findByStudyGroupIdAndAccountId(groupId, accountId).isPresent()) {
                throw new ApiException(400, "User is already a member of the group.");
            }
            if (groupMembers.size() == group.getMemberLimited()) {
                throw new ApiException(400, "Group has reached its member limit (" + group.getMemberLimited() + ").");
            }

            GroupMember member = new GroupMember(null, accountId, group);
            member.setRole(GroupMemberRole.MEMBER);
            memberRepository.save(member);
            joinRequestRepository.delete(joinRequest);
            MessageEventDto messageEventDto = new MessageEventDto(
                    groupId,
                    accountId,
                    "JOIN_APPROVE",
                    null,
                    "User " + accountId + " has been approved to join the group " + groupId
            );

            kafkaProducerService.sendMessageEvent(messageEventDto, group,false);

            log.info("Join request {} approved for user {} in group {}", joinRequestId, accountId, groupId);
        } catch (Exception e) {
            log.error("Failed to approve join request: {}", e.getMessage(), e);
            throw new ApiException(500, "Failed to approve join request: " + e.getMessage());
        }
    }

    @Transactional
    @Override
    public void rejectJoinRequest(Long joinRequestId) {
        try {
            JoinRequest joinRequest = joinRequestRepository.findById(joinRequestId)
                    .orElseThrow(() -> new ApiException(404, "Join request with ID " + joinRequestId + " not found."));

            if (joinRequest.getStatus() != JoinRequest.RequestStatus.PENDING) {
                throw new ApiException(400, "Join request has already been processed (status: " + joinRequest.getStatus() + ").");
            }

            StudyGroup group = joinRequest.getStudyGroup();
            if (group == null) {
                throw new ApiException(404, "Associated group not found for join request ID " + joinRequestId + ".");
            }

            Long groupId = group.getId();
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            AccountDto currentUser = (AccountDto) authentication.getPrincipal();

            List<GroupMember> groupMembers = groupMemberRepository.findByStudyGroupId(groupId);
            List<Long> adminIds = groupMembers.stream()
                    .filter(groupMember -> groupMember.getRole().equals(GroupMemberRole.ADMIN))
                    .map(GroupMember::getAccountId)
                    .toList();
            if(!group.getOwnerId().equals(currentUser.getAccountId())){
                if(!adminIds.contains(currentUser.getAccountId())){
                    throw new ApiException(403, "Only owner or admin can approve join requests.");
                }
            }

            joinRequestRepository.delete(joinRequest);

            Long accountId = joinRequest.getAccountId();
            String message = "You are not approved to join the group " + group.getName();
            kafkaProducerService.sendNotificationRejectOrRemoveGroup(accountId,"JOIN_REJECT", message);

            log.info("Join request {} rejected for user {} in group {}", joinRequestId, accountId, groupId);
        }catch (Exception e) {
            log.error("Failed to reject join request: {}", e.getMessage(), e);
            throw new ApiException(500, "Failed to reject join request: " + e.getMessage());
        }
    }

    @Transactional
    @Override
    public void removeMember(Long groupId, Long userId) {
        try {
            StudyGroup group = groupRepository.findById(groupId)
                    .orElseThrow(() -> new ApiException(404, "Group not found."));

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            AccountDto currentUser = (AccountDto) authentication.getPrincipal();

            if (!group.getOwnerId().equals(currentUser.getAccountId()) && !userId.equals(currentUser.getAccountId())) {
                GroupMember membership = memberRepository.findByStudyGroupIdAndAccountId(groupId, currentUser.getAccountId())
                        .orElseThrow(() -> new ApiException(403, "User is not a member of this group"));
                if (!membership.getRole().equals(GroupMemberRole.ADMIN)) {
                    throw new ApiException(403, "Only owner or admin can remove members, or user can leave group.");
                }
            }

            GroupMember member = memberRepository.findByStudyGroupIdAndAccountId(groupId, userId)
                    .orElseThrow(() -> new ApiException(404, "Member not found."));

            memberRepository.delete(member);
            String message = "You have been forced to leave the group " + group.getName();
            kafkaProducerService.sendNotificationRejectOrRemoveGroup(member.getAccountId(),"LEAVE_MEMBER", message);
        } catch (Exception e) {
            log.error("Failed to remove member: {}", e.getMessage(), e);
            throw new ApiException(500, "Failed to remove member: " + e.getMessage());
        }
    }

    @Override
    public void leaveGroup(Long groupId, AccountDto accountDto) throws Exception {
        try{
            StudyGroup group = groupRepository.findById(groupId)
                    .orElseThrow(() -> new ApiException(404, "Group not found."));
            GroupMember member = memberRepository.findByStudyGroupIdAndAccountId(groupId, accountDto.getAccountId())
                    .orElseThrow(() -> new ApiException(404, "Member not found."));
            memberRepository.delete(member);
            String message = "You have been to leave the group " + group.getName();
            kafkaProducerService.sendNotificationRejectOrRemoveGroup(member.getAccountId(),"LEAVE_MEMBER", message);
        }catch (Exception e){
            throw new Exception(e);
        }
    }

    @Transactional
    @Override
    public void pinMessage(Long messageId) {
        try {
            Message message = messageRepository.findById(messageId)
                    .orElseThrow(() -> new ApiException(404, "Message not found."));

            StudyGroup group = message.getGroup();

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            AccountDto currentUser = (AccountDto) authentication.getPrincipal();

            if (!group.getOwnerId().equals(currentUser.getAccountId())) {
                throw new ApiException(403, "Only owner can pin messages.");
            }

            message.setIsPinned(true);
            messageRepository.save(message);

            kafkaProducerService.sendMessageEvent(new MessageEventDto(group.getId(), currentUser.getAccountId(), "pin_message", messageId, null));
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
                    .orElseThrow(() -> new ApiException(404, "Message not found."));

            StudyGroup group = message.getGroup();

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            AccountDto currentUser = (AccountDto) authentication.getPrincipal();

            if (!group.getOwnerId().equals(currentUser.getAccountId())) {
                throw new ApiException(403, "Only owner can unpin messages.");
            }

            message.setIsPinned(false);
            messageRepository.save(message);

            kafkaProducerService.sendMessageEvent(new MessageEventDto(group.getId(), currentUser.getAccountId(), "unpin_message", messageId, null));
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
                    .orElseThrow(() -> new ApiException(404, "Group not found."));

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            AccountDto currentUser = (AccountDto) authentication.getPrincipal();

            if (!group.getOwnerId().equals(currentUser.getAccountId())) {
                GroupMember requesterMembership = memberRepository.findByStudyGroupIdAndAccountId(groupId, currentUser.getAccountId())
                        .orElseThrow(() -> new ApiException(403, "User is not a member of this group."));
                if (!requesterMembership.getRole().equals(GroupMemberRole.OWNER)) {
                    throw new ApiException(403, "Only owner can delete group.");
                }
            }

            messageRepository.deleteByGroupId(groupId);
            memberRepository.deleteByStudyGroupId(groupId);
            groupRepository.delete(group);

            kafkaProducerService.sendMessageEvent(new MessageEventDto(groupId, currentUser.getAccountId(), "delete", null, null));
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
                return Page.empty(pageable);
            }

            return membersPage.map(member -> {
                try {
                    AccountDto account = identityClient.getAccountId(member.getAccountId()).getData();
                    if (account == null) {
                        throw new ApiException(404, "Account not found for member ID: " + member.getAccountId());
                    }
                    return new GroupResponse(member.getAccountId(), account.getUsername(), account.getEmail(), account.getProfilePicture());
                } catch (Exception e) {
                    log.error("Error fetching account details: {}", e.getMessage(), e);
                    throw new ApiException(500, "Error fetching account details for member ID " + member.getAccountId());
                }
            });
        } catch (Exception e) {
            log.error("Failed to list members: {}", e.getMessage(), e);
            throw new ApiException(500, "Failed to list members: " + e.getMessage());
        }
    }

    @Transactional
    @Override
    public StudyGroup editGroup(Long groupId, String groupName, String description, Long subjectId, String picture, Integer memberLimited, Boolean isPrivate, MultipartFile file) {
        try {
            StudyGroup group = groupRepository.findById(groupId)
                    .orElseThrow(() -> new ApiException(400, "Group not found."));

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            AccountDto currentUser = (AccountDto) authentication.getPrincipal();

            if (!group.getOwnerId().equals(currentUser.getAccountId())) {
                GroupMember membership = memberRepository.findByStudyGroupIdAndAccountId(groupId, currentUser.getAccountId())
                        .orElseThrow(() -> new ApiException(400, "User is not a member of this group."));
                if (!membership.getRole().equals(GroupMemberRole.ADMIN)) {
                    throw new ApiException(400, "Only owner or admin can edit group.");
                }
            }

            if (groupName != null) {
                group.setName(groupName);
            }
            if (description != null) {
                group.setDescription(description);
            }
            if (subjectId != null) {
                SubjectDto subject = fetchSubjectById(subjectId);
                if (subject == null) {
                    throw new ApiException(400, "Subject with ID " + subjectId + " not found.");
                }
                group.setSubjectId(subjectId);
            }
            if(file!=null){
                if(group.getPicture()!=null){
                    String originalFileName = file.getOriginalFilename();
                    String fileName= firebaseService.generateFileName(originalFileName);
                    firebaseService.delete(fileName);
                }
                try {
                    String originalFileName = file.getOriginalFilename();
                    if (originalFileName.isBlank()) {
                        throw new ApiException(ErrorCode.BAD_REQUEST.getStatusCode().value(),"Invalid file name.");
                    }

                    BufferedImage bufferedImage = ImageIO.read(file.getInputStream());
                    CompletableFuture<String> uploadFuture = firebaseService.save(bufferedImage, originalFileName);
                    String avatarGroup = uploadFuture.get();
                    System.out.println("Avatar group URL: " + avatarGroup);
                    group.setPicture(avatarGroup);

                } catch (IOException e) {
                    throw new ApiException(ErrorCode.BAD_GATEWAY.getStatusCode().value(),"Error processing profile picture: " + e.getMessage());

                } catch (InterruptedException | ExecutionException e) {
                    Thread.currentThread().interrupt();
                    throw new ApiException(ErrorCode.BAD_GATEWAY.getStatusCode().value(),"Error uploading profile picture: " + e.getMessage());
                }
            }
            if (memberLimited > 0) {
                List<GroupMember> groupMembers = groupMemberRepository.findByStudyGroupId(groupId);
                if(memberLimited<groupMembers.size()){
                    throw new ApiException(ErrorCode.BAD_REQUEST.getStatusCode().value(),"Member limit not valid");
                }
                group.setMemberLimited(memberLimited);
            }

            group.setIsPrivate(isPrivate);
            String splitUsername;
            if(currentUser.getUsername().contains("@")){
                splitUsername = currentUser.getEmail().split("@")[0];
            }else{
                splitUsername = currentUser.getUsername();
            }

            StudyGroup savedGroup = groupRepository.save(group);
            MessageEventDto messageEventDto = new MessageEventDto();
            messageEventDto.setGroupId(groupId);
            messageEventDto.setSenderId(currentUser.getAccountId());
            messageEventDto.setMessageId(null);
            messageEventDto.setEventType("UPDATE_GROUP");
            messageEventDto.setContent(splitUsername + " vừa cập nhật thông tin nhóm!");

            kafkaProducerService.sendMessageEvent(messageEventDto,group,true);

            return savedGroup;
        } catch (Exception e) {
            log.error("Failed to edit group: {}", e.getMessage(), e);
            throw new ApiException(500, "Failed to edit group: " + e.getMessage());
        }
    }

    @Transactional
    @Override
    public StudyGroup updatePrivacySetting(Long groupId, boolean isPrivate) {
        try {
            StudyGroup group = groupRepository.findById(groupId)
                    .orElseThrow(() -> new ApiException(404, "Group not found."));

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            AccountDto currentUser = (AccountDto) authentication.getPrincipal();

            if (!group.getOwnerId().equals(currentUser.getAccountId())) {
                throw new ApiException(403, "Only owner can change privacy settings.");
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
        }catch (Exception e) {
            log.error("Failed to update privacy setting: {}", e.getMessage(), e);
            throw new ApiException(500, "Failed to update privacy setting: " + e.getMessage());
        }
    }

    @Override
    public Page<MessageResponse> getPinnedMessages(Long groupId, Pageable pageable) {
        try {
            if (!pageable.getSort().isSorted()) {
                pageable = PageRequest.of(
                        pageable.getPageNumber(),
                        pageable.getPageSize(),
                        Sort.by(Sort.Direction.DESC, "createdAt")
                );
            }

            Page<Message> pinnedMessages = messageRepository.findByGroupIdAndIsPinnedTrue(groupId, pageable);

            if (pinnedMessages.isEmpty()) {
                return Page.empty(pageable);
            }

            Set<Long> senderIds = pinnedMessages.getContent().stream()
                    .map(Message::getSenderId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());

            Map<Long, AccountDto> userDetailsMap = senderIds.isEmpty() ? Collections.emptyMap() : getAccountsWithCache(senderIds);


            return pinnedMessages.map(message -> {
                MessageResponse dto = new MessageResponse();
                dto.setMessageId(message.getId());
                dto.setGroupId(message.getGroup().getId());
                dto.setSenderId(message.getSenderId());
                dto.setContent(message.getContent());
                dto.setCreatedAt(message.getCreatedAt());

                AccountDto account = userDetailsMap.get(message.getSenderId());
                if (account == null) {
                    log.warn("Account not found for sender ID: {}", message.getSenderId());
                    dto.setUsername("Unknown");
                    dto.setProfilePicture(null);
                } else {
                    String fullName = (account.getFirstName() != null ? account.getFirstName() : "") +
                            (account.getLastName() != null ? " " + account.getLastName() : "");
                    dto.setUsername(fullName.trim().isEmpty() ? account.getEmail().split("@")[0] : fullName.trim());
                    dto.setProfilePicture(account.getProfilePicture());
                }
                return dto;
            });
        } catch (Exception e) {
            log.error("Failed to fetch pinned messages: {}", e.getMessage(), e);
            throw new ApiException(500, "Failed to fetch pinned messages: " + e.getMessage());
        }
    }

    @Override
    public Page<MessageResponse> getGroupMessages(Long groupId, Pageable pageable) {
        try {
            // Nếu pageable không có sort, đặt mặc định là sort theo createdAt DESC
            if (!pageable.getSort().isSorted()) {
                pageable = PageRequest.of(
                        pageable.getPageNumber(),
                        pageable.getPageSize(),
                        Sort.by(Sort.Direction.DESC, "createdAt")
                );
            }

            Page<Message> messages = messageRepository.findByGroupId(groupId, pageable);
            if (messages.isEmpty()) {
                return Page.empty(pageable);
            }

            Set<Long> senderIds = messages.getContent().stream()
                    .map(Message::getSenderId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());

            Map<Long, AccountDto> userDetailsMap = senderIds.isEmpty() ? Collections.emptyMap() : getAccountsWithCache(senderIds);

            return messages.map(message -> {
                MessageResponse dto = new MessageResponse();
                dto.setMessageId(message.getId());
                dto.setGroupId(message.getGroup().getId());
                dto.setSenderId(message.getSenderId());
                dto.setContent(message.getContent());
                dto.setCreatedAt(message.getCreatedAt());

                AccountDto account = userDetailsMap.get(message.getSenderId());
                if (account == null) {
                    log.warn("Account not found for sender ID: {}", message.getSenderId());
                    dto.setUsername("Unknown");
                    dto.setProfilePicture(null);
                } else {
                    String fullName = (account.getFirstName() != null ? account.getFirstName() : "") +
                            (account.getLastName() != null ? " " + account.getLastName() : "");
                    dto.setUsername(fullName.trim().isEmpty() ? account.getEmail().split("@")[0] : fullName.trim());
                    dto.setProfilePicture(account.getProfilePicture());
                }
                return dto;
            });
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
                    .orElseThrow(() -> new ApiException(404, "Group not found."));

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            AccountDto sender = (AccountDto) authentication.getPrincipal();

            if (!isMember(groupId, sender.getAccountId())) {
                throw new ApiException(403, "User is not a member of this group.");
            }

            Message message = new Message();
            message.setSenderId(sender.getAccountId());
            message.setContent("Shared document: " + shareUrl);
            message.setIsPinned(false);
            message.setDocumentLink(true);
            message.setDocumentId(documentId);
            message.setGroup(group);

            Message savedMessage = messageRepository.save(message);

            kafkaProducerService.sendMessageEvent(new MessageEventDto(groupId, sender.getAccountId(), "share_document", savedMessage.getId(), null));

            return savedMessage;
        }catch (Exception e) {
            log.error("Failed to share document: {}", e.getMessage(), e);
            throw new ApiException(500, "Failed to share document: " + e.getMessage());
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
                return Collections.emptyList();
            }

            return memberships.stream()
                    .map(GroupMember::getStudyGroup)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Failed to find user groups: {}", e.getMessage(), e);
            throw new ApiException(500, "Failed to find user groups: " + e.getMessage());
        }
    }

    @Transactional
    @Override
    public void transferOwnership(Long groupId, Long newOwnerId) {
        try {
            StudyGroup group = groupRepository.findById(groupId)
                    .orElseThrow(() -> new ApiException(404, "Group not found."));

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            AccountDto currentOwner = (AccountDto) authentication.getPrincipal();

            if (!group.getOwnerId().equals(currentOwner.getAccountId())) {
                throw new ApiException(403, "Only current owner can transfer ownership.");
            }

            if (!isMember(groupId, newOwnerId)) {
                throw new ApiException(404, "New owner must be a member of the group.");
            }

            group.setOwnerId(newOwnerId);
            groupRepository.save(group);

            Map<String, Object> additionalData = Map.of(
                    "newOwnerId", newOwnerId,
                    "previousOwnerId", currentOwner.getAccountId()
            );

            kafkaProducerService.sendMessageEvent(new MessageEventDto(groupId, currentOwner.getAccountId(), "ownership_transfer", null, additionalData.toString()));
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
                    .orElseThrow(() -> new ApiException(404, "Message not found."));

            StudyGroup group = message.getGroup();

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            AccountDto currentUser = (AccountDto) authentication.getPrincipal();

            if (!message.getSenderId().equals(currentUser.getAccountId()) && !group.getOwnerId().equals(currentUser.getAccountId())) {
                throw new ApiException(403, "Only message sender or group owner can delete messages.");
            }

            messageRepository.delete(message);

            kafkaProducerService.sendMessageEvent(new MessageEventDto(group.getId(), currentUser.getAccountId(), "delete_message", messageId, null));
        } catch (Exception e) {
            log.error("Failed to delete message: {}", e.getMessage(), e);
            throw new ApiException(500, "Failed to delete message: " + e.getMessage());
        }
    }

    @Override
    public List<StudyGroup> searchGroups(String keyword) {
        try {
            List<StudyGroup> groups = groupRepository.findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(keyword, keyword);
            return groups.isEmpty() ? Collections.emptyList() : groups;
        } catch (Exception e) {
            log.error("Failed to search groups: {}", e.getMessage(), e);
            throw new ApiException(500, "Failed to search groups: " + e.getMessage());
        }
    }

    @Override
    public boolean isGroupOwner(Long groupId, Long userId) {
        try {
            StudyGroup group = groupRepository.findById(groupId)
                    .orElseThrow(() -> new ApiException(404, "Group not found."));
            return group.getOwnerId().equals(userId);
        } catch (Exception e) {
            log.error("Failed to check group ownership: {}", e.getMessage(), e);
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
                return Collections.emptyList();
            }

            return groupMembers.stream()
                    .map(groupMember -> {
                        StudyGroup group = groupMember.getStudyGroup();
                        SubjectDto subject = fetchSubjectById(group.getSubjectId());
                        if (subject == null) {
                            throw new ApiException(404, "Subject with ID " + group.getSubjectId() + " not found.");
                        }

                        StudyGroupEventDto dto = new StudyGroupEventDto();
                        dto.setGroupId(group.getId());
                        dto.setGroupName(group.getName());
                        dto.setDescription(group.getDescription());
                        dto.setPicture(group.getPicture());
                        dto.setSubjectName(subject.getSubjectName());
                        dto.setUserId(group.getOwnerId());
                        dto.setMemberLimited(group.getMemberLimited());
                        dto.setMemberCount(groupRepository.getMemberCount(group.getId()));
                        return dto;
                    })
                    .collect(Collectors.toList());
        }catch (Exception e) {
            log.error("Failed to fetch groups by user ID: {}", e.getMessage(), e);
            throw new ApiException(500, "Failed to fetch groups by user ID: " + e.getMessage());
        }
    }

    @Transactional
    @Override
    public void setRoleForMember(Long groupId, Long userId, GroupMemberRole role) {
        if (groupId == null || groupId <= 0) {
            throw new ApiException(400, "Invalid group ID");
        }
        if (userId == null || userId <= 0) {
            throw new ApiException(400, "Invalid user ID");
        }
        if (role == null) {
            throw new ApiException(400, "Role cannot be null");
        }

        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            AccountDto currentUser = (AccountDto) authentication.getPrincipal();
            StudyGroup group = groupRepository.findById(groupId)
                    .orElseThrow(() -> new ApiException(404, "Group not found"));
            if (!group.getOwnerId().equals(currentUser.getAccountId())) {
                GroupMember membership = memberRepository.findByStudyGroupIdAndAccountId(groupId, currentUser.getAccountId())
                        .orElseThrow(() -> new ApiException(403, "User is not a member of this group"));
                if (!membership.getRole().equals(GroupMemberRole.ADMIN)) {
                    throw new ApiException(403, "Only owner or admin can set member roles");
                }
            }

            GroupMember member = memberRepository.findByStudyGroupIdAndAccountId(groupId, userId)
                    .orElseThrow(() -> new ApiException(404, "Member not found"));
            member.setRole(role);
            memberRepository.save(member);

            String cacheKey = "group:member:" + groupId + ":" + userId;
            redisService.deleteData(cacheKey);
            log.info("Updated role {} for user {} in group {}", role, userId, groupId);

            AccountDto user = identityClient.getAccountId(userId).getData();
            kafkaProducerService.sendRoleUpdateNotification(groupId, userId, role, group.getName(), user);

        } catch (DataIntegrityViolationException e) {
            log.error("Data integrity violation for group {} user {}: {}", groupId, userId, e.getMessage(), e);
            throw new ApiException(400, "Invalid data provided for role update");
        } catch (Exception e) {
            log.error("Failed to set role {} for user {} in group {}: {}", role, userId, groupId, e.getMessage(), e);
            throw new ApiException(500, "Internal server error");
        }
    }
}