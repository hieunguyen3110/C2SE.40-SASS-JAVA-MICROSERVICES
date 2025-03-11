package org.com.studygroupservice.service;


import org.com.studygroupservice.dto.request.CreateGroupRequest;
import org.com.studygroupservice.dto.response.GroupResponse;
import org.com.studygroupservice.entity.StudyGroup;
import org.com.studygroupservice.entity.Message;

import java.util.List;

public interface StudyGroupService {
    StudyGroup createGroup(StudyGroup group, Long ownerId);
    StudyGroup getGroupDetails(Long groupId);
    Message sendMessage(Long groupId, Long senderId, String content);
    void removeMember(Long groupId, Long userId, Long requesterId);
    void pinMessage(Long messageId, Long requesterId);
    void deleteGroup(Long groupId, Long requesterId);
    List<GroupResponse> listMembers(Long groupId);
    StudyGroup editGroup(Long groupId, StudyGroup updatedGroup, Long requesterId);
    List<Message> getPinnedMessages(Long groupId);
    Message shareDocument(Long groupId, Long senderId, String documentId);
}
