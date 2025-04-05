package org.com.studygroupservice.service;

import org.com.studygroupservice.dto.request.CreateGroupRequest;
import org.com.studygroupservice.dto.response.GroupResponse;
import org.com.studygroupservice.dto.response.SubjectDto;
import org.com.studygroupservice.entity.Message;
import org.com.studygroupservice.entity.StudyGroup;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface StudyGroupService {

    @Transactional
    StudyGroup createGroup(String groupName, String description, Long subjectId, boolean isPrivate, int memberLimited, List<Long> memberIds);

    List<SubjectDto> searchSubjectsByName(String subjectName);

    // Lấy danh sách SubjectDto từ Redis, fallback sang document-service nếu cần
    List<SubjectDto> fetchSubjects();

    StudyGroup getGroupDetails(Long groupId);

    void sendMessage(Long groupId, String content);

    @Transactional
    void addMember(Long groupId, Long userId);

    @Transactional
    void joinGroup(Long groupId, Long userId);

    void removeMember(Long groupId, Long userId);

    @Transactional
    void pinMessage(Long messageId);

    @Transactional
    void unpinMessage(Long messageId);

    @Transactional
    void deleteGroup(Long groupId);

    Page<GroupResponse> listMembers(Long groupId, Pageable pageable);

    @Transactional
    StudyGroup editGroup(Long groupId, String groupName, String description, Long subjectId, String picture, int memberLimited);

    @Transactional
    StudyGroup updatePrivacySetting(Long groupId, boolean isPrivate);

    Page<Message> getPinnedMessages(Long groupId, Pageable pageable);

    Page<Message> getGroupMessages(Long groupId, Pageable pageable);

    @Transactional
    Message shareDocumentToGroup(Long groupId, String documentId, String shareUrl);

    List<StudyGroup> findUserGroups(Long userId);

    void transferOwnership(Long groupId, Long newOwnerId);

    @Transactional
    void deleteMessage(Long messageId);

    List<StudyGroup> searchGroups(String keyword);

    boolean isGroupOwner(Long groupId, Long userId);
}