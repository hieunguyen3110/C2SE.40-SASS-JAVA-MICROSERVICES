package org.com.studygroupservice.service;

import org.com.studygroupservice.dto.request.CreateGroupRequest;
import org.com.studygroupservice.dto.response.*;
import org.com.studygroupservice.entity.GroupMember;
import org.com.studygroupservice.entity.Message;
import org.com.studygroupservice.entity.StudyGroup;
import org.com.studygroupservice.enums.GroupMemberRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface StudyGroupService {

    @Transactional
    StudyGroup createGroup(String groupName, String description, Long subjectId, Boolean isPrivate, Integer memberLimited, List<Long> memberIds);

    List<SubjectDto> searchSubjectsByName(String subjectName);

    List<SubjectDto> fetchSubjects();

    StudyGroupEventDto getGroupDetails(Long groupId);

    @Transactional
    void sendMessage(Long groupId, String content, Long senderId);

    @Transactional
    void addMember(Long groupId, Long userId);

    @Transactional
    StudyGroupEventDto joinGroup(Long groupId, AccountDto accountDto) throws Exception;

    void removeMember(Long groupId, Long userId);
    void leaveGroup(Long groupId, AccountDto accountDto) throws Exception;

    @Transactional
    void pinMessage(Long messageId);

    @Transactional
    void unpinMessage(Long messageId);

    @Transactional
    void deleteGroup(Long groupId);

    Page<GroupResponse> listMembers(Long groupId, Pageable pageable);

    @Transactional
    StudyGroup editGroup(Long groupId, String groupName, String description, Long subjectId, String picture, Integer memberLimited, Boolean isPrivate, MultipartFile file);

    @Transactional
    StudyGroup updatePrivacySetting(Long groupId, boolean isPrivate);

    Page<MessageResponse> getPinnedMessages(Long groupId, Pageable pageable);

    Page<MessageResponse> getGroupMessages(Long groupId, Pageable pageable);

    @Transactional
    Message shareDocumentToGroup(Long groupId, String documentId, String shareUrl);

    List<StudyGroup> findUserGroups(Long userId);

    void transferOwnership(Long groupId, Long newOwnerId);

    @Transactional
    void deleteMessage(Long messageId);

    List<StudyGroup> searchGroups(String keyword);

    boolean isGroupOwner(Long groupId, Long userId);

    List<StudyGroupEventDto> getGroupsByUserId();

    void approveJoinRequest(Long joinRequestId);

    void rejectJoinRequest(Long joinRequestId);

    void setRoleForMember(Long groupId, Long userId, String role);
}