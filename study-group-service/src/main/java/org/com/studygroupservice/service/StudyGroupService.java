package org.com.studygroupservice.service;

import org.com.studygroupservice.dto.response.GroupResponse;
import org.com.studygroupservice.entity.StudyGroup;
import org.com.studygroupservice.entity.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface StudyGroupService {

    StudyGroup createGroup(StudyGroup group, Long ownerId);

    StudyGroup getGroupDetails(Long groupId);

    void sendMessage(Long groupId, Long senderId, String content);

    void addMember(Long groupId, Long userId);

    void joinGroup(Long groupId, Long userId);

    void removeMember(Long groupId, Long userId, Long requesterId);

    void pinMessage(Long messageId, Long requesterId);

    void unpinMessage(Long messageId, Long requesterId);

    void deleteGroup(Long groupId, Long requesterId);

    Page<GroupResponse> listMembers(Long groupId, Pageable pageable);

    StudyGroup editGroup(Long groupId, StudyGroup updatedGroup, Long requesterId);

    Page<Message> getPinnedMessages(Long groupId, Pageable pageable);

    Page<Message> getGroupMessages(Long groupId, Pageable pageable);

    List<StudyGroup> findUserGroups(Long userId);

    void transferOwnership(Long groupId, Long newOwnerId, Long currentOwnerId);

    void deleteMessage(Long messageId, Long requesterId);

    List<StudyGroup> searchGroups(String keyword);

    boolean isGroupOwner(Long groupId, Long userId);

    Message shareDocumentToGroup(Long groupId, Long senderId, String documentId, String shareUrl) throws Exception;
}