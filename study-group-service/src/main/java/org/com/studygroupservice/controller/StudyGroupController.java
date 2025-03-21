package org.com.studygroupservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.com.studygroupservice.dto.request.*;
import org.com.studygroupservice.dto.response.ApiResponse;
import org.com.studygroupservice.dto.response.GroupResponse;
import org.com.studygroupservice.entity.Message;
import org.com.studygroupservice.entity.StudyGroup;
import org.com.studygroupservice.helpers.CreateApiResponse;
import org.com.studygroupservice.service.StudyGroupService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class StudyGroupController {
    private final StudyGroupService groupService;

    @PostMapping
    public ApiResponse<StudyGroup> createGroup(@Valid @RequestBody CreateGroupRequest request) {
        StudyGroup group = groupService.createGroup(request.getGroupName(), request.isPrivate(), request.getMemberIds());
        return CreateApiResponse.createResponse(group, true);
    }

    @GetMapping("/{groupId}")
    public ApiResponse<StudyGroup> getGroupDetails(@PathVariable Long groupId) {
        StudyGroup group = groupService.getGroupDetails(groupId);
        return CreateApiResponse.createResponse(group, false);
    }

    @MessageMapping("/chat.sendMessage")
    public void sendMessage(@Payload ChatMessage chatMessage) {
        groupService.sendMessage(chatMessage.getGroupId(), chatMessage.getContent());
    }

    @DeleteMapping("/{groupId}/members/{userId}")
    public ApiResponse<Void> removeMember(@PathVariable Long groupId, @PathVariable Long userId) {
        groupService.removeMember(groupId, userId);
        return CreateApiResponse.createResponse(null, false);
    }

    @PostMapping("/messages/{messageId}/pin")
    public ApiResponse<Void> pinMessage(@PathVariable Long messageId) {
        groupService.pinMessage(messageId);
        return CreateApiResponse.createResponse(null, false);
    }

    @DeleteMapping("/{groupId}")
    public ApiResponse<Void> deleteGroup(@PathVariable Long groupId) {
        groupService.deleteGroup(groupId);
        return CreateApiResponse.createResponse(null, false);
    }

    @GetMapping("/{groupId}/members")
    public ApiResponse<Page<GroupResponse>> listMembers(@PathVariable Long groupId,
                                                        @RequestParam(defaultValue = "0") int page,
                                                        @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<GroupResponse> members = groupService.listMembers(groupId, pageable);
        return CreateApiResponse.createResponse(members, false);
    }

    @PutMapping("/{groupId}")
    public ApiResponse<StudyGroup> editGroup(@PathVariable Long groupId,
                                             @Valid @RequestBody CreateGroupRequest request) {
        StudyGroup updatedGroup = groupService.editGroup(groupId, request.getGroupName());
        return CreateApiResponse.createResponse(updatedGroup, false);
    }

    @GetMapping("/{groupId}/pinned-messages")
    public ApiResponse<Page<Message>> getPinnedMessages(@PathVariable Long groupId,
                                                        @RequestParam(defaultValue = "0") int page,
                                                        @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Message> pinnedMessages = groupService.getPinnedMessages(groupId, pageable);
        return CreateApiResponse.createResponse(pinnedMessages, false);
    }

    @PostMapping("/{groupId}/documents")
    public ApiResponse<Message> shareDocument(@PathVariable Long groupId,
                                              @Valid @RequestBody ShareDocumentRequest request) throws Exception {
        Message message = groupService.shareDocumentToGroup(groupId, request.getDocumentId(), request.getShareUrl());
        return CreateApiResponse.createResponse(message, true);
    }
}