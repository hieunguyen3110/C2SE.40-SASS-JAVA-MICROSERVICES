package org.com.studygroupservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.com.studygroupservice.dto.request.*;
import org.com.studygroupservice.dto.response.*;
import org.com.studygroupservice.entity.Message;
import org.com.studygroupservice.entity.StudyGroup;
import org.com.studygroupservice.enums.ErrorCode;
import org.com.studygroupservice.exception.ApiException;
import org.com.studygroupservice.helpers.CreateApiResponse;
import org.com.studygroupservice.service.StudyGroupService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class StudyGroupController {
    private final StudyGroupService groupService;

    @PostMapping("/create")
    public ApiResponse<StudyGroup> createGroup(@Valid @RequestBody CreateGroupRequest request) {
        StudyGroup group = groupService.createGroup(
                request.getGroupName(),
                request.getDescription(),
                request.getSubjectId(),
                request.isPrivate(),
                request.getMemberLimited(),
                request.getMemberIds()
        );
        return CreateApiResponse.createResponse(group, true);
    }

    @GetMapping("/subjects")
    public ApiResponse<List<SubjectDto>> searchSubjects(@RequestParam String subjectName) {
        List<SubjectDto> subjects = groupService.searchSubjectsByName(subjectName);
        return CreateApiResponse.createResponse(subjects, false);
    }

    @GetMapping("/detail-group/{groupId}")
    public ApiResponse<StudyGroupEventDto> getGroupDetails(@PathVariable Long groupId) {
        StudyGroupEventDto group = groupService.getGroupDetails(groupId);
        return CreateApiResponse.createResponse(group, false);
    }

    @MessageMapping("/app.sendMessage")
    public void sendMessage(@Payload ChatMessage chatMessage) {
        groupService.sendMessage(chatMessage.getGroupId(), chatMessage.getContent(), chatMessage.getSenderId());
    }

    @PostMapping("/{groupId}/members")
    public ApiResponse<Void> addMember(@PathVariable Long groupId, @RequestParam Long userId) {
        groupService.addMember(groupId, userId);
        return CreateApiResponse.createResponse(null, false);
    }

    @PostMapping("/{groupId}/join")
    public ApiResponse<String> joinGroup(@PathVariable Long groupId) throws Exception {
        Authentication authentication= SecurityContextHolder.getContext().getAuthentication();
        if(!(authentication instanceof AnonymousAuthenticationToken)){
            AccountDto accountDto= (AccountDto) authentication.getPrincipal();
            groupService.joinGroup(groupId, accountDto.getAccountId());
            return CreateApiResponse.createResponse("Send request join group successful", false);
        }else{
            throw new ApiException(ErrorCode.BAD_REQUEST.getStatusCode().value(),"Token is expires");
        }

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

    @PostMapping("/messages/{messageId}/unpin")
    public ApiResponse<Void> unpinMessage(@PathVariable Long messageId) {
        groupService.unpinMessage(messageId);
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
        StudyGroup updatedGroup = groupService.editGroup(
                groupId,
                request.getGroupName(),
                request.getDescription(),
                request.getSubjectId(),
                request.getPicture(),
                request.getMemberLimited()
        );
        return CreateApiResponse.createResponse(updatedGroup, false);
    }

    @PutMapping("/{groupId}/privacy")
    public ApiResponse<StudyGroup> updatePrivacySetting(@PathVariable Long groupId,
                                                        @RequestParam boolean isPrivate) {
        StudyGroup updatedGroup = groupService.updatePrivacySetting(groupId, isPrivate);
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

    @GetMapping("/{groupId}/messages")
    public ApiResponse<Page<Message>> getGroupMessages(@PathVariable Long groupId,
                                                       @RequestParam(defaultValue = "0") int page,
                                                       @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Message> messages = groupService.getGroupMessages(groupId, pageable);
        return CreateApiResponse.createResponse(messages, false);
    }

    @PostMapping("/{groupId}/documents")
    public ApiResponse<Message> shareDocument(@PathVariable Long groupId,
                                              @Valid @RequestBody ShareDocumentRequest request) {
        Message message = groupService.shareDocumentToGroup(groupId, request.getDocumentId(), request.getShareUrl());
        return CreateApiResponse.createResponse(message, true);
    }

    @GetMapping("/user/{userId}")
    public ApiResponse<List<StudyGroup>> findUserGroups(@PathVariable Long userId) {
        List<StudyGroup> groups = groupService.findUserGroups(userId);
        return CreateApiResponse.createResponse(groups, false);
    }

    @PostMapping("/{groupId}/transfer-ownership")
    public ApiResponse<Void> transferOwnership(@PathVariable Long groupId, @RequestParam Long newOwnerId) {
        groupService.transferOwnership(groupId, newOwnerId);
        return CreateApiResponse.createResponse(null, false);
    }

    @DeleteMapping("/messages/{messageId}")
    public ApiResponse<Void> deleteMessage(@PathVariable Long messageId) {
        groupService.deleteMessage(messageId);
        return CreateApiResponse.createResponse(null, false);
    }

    @GetMapping("/groupMembers")
    public ApiResponse<List<StudyGroupEventDto>> getGroupsByUserId() {
        List<StudyGroupEventDto> groups = groupService.getGroupsByUserId();
        return CreateApiResponse.createResponse(groups, false);
    }

    @PostMapping("/join-requests/{joinRequestId}/approve")
    public ApiResponse<Void> approveJoinRequest(@PathVariable Long joinRequestId) {
        groupService.approveJoinRequest(joinRequestId);
        return CreateApiResponse.createResponse(null, false);
    }

    @PostMapping("/join-requests/{joinRequestId}/reject")
    public ApiResponse<Void> rejectJoinRequest(@PathVariable Long joinRequestId) {
        groupService.rejectJoinRequest(joinRequestId);
        return CreateApiResponse.createResponse(null, false);
    }

}