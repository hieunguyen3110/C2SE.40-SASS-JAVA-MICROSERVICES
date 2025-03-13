package org.com.studygroupservice.controller;

import lombok.RequiredArgsConstructor;
import org.com.studygroupservice.dto.request.ChatMessage;
import org.com.studygroupservice.dto.response.GroupResponse;
import org.com.studygroupservice.entity.Message;
import org.com.studygroupservice.entity.StudyGroup;
import org.com.studygroupservice.service.StudyGroupService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/study-groups")
@RequiredArgsConstructor
public class StudyGroupController {
    private final StudyGroupService groupService;

    @PostMapping
    public ResponseEntity<StudyGroup> createGroup(@RequestBody StudyGroup group,
                                                  @RequestHeader("User-Id") Long userId) {
        return ResponseEntity.ok(groupService.createGroup(group, userId));
    }

    @GetMapping("/{groupId}")
    public ResponseEntity<StudyGroup> getGroupDetails(@PathVariable Long groupId) {
        return ResponseEntity.ok(groupService.getGroupDetails(groupId));
    }

    @MessageMapping("/chat.sendMessage")
    public void sendMessage(@Payload ChatMessage chatMessage) {
        groupService.sendMessage(chatMessage.getGroupId(), chatMessage.getSenderId(), chatMessage.getContent());
    }

    @DeleteMapping("/{groupId}/members/{userId}")
    public ResponseEntity<Void> removeMember(@PathVariable Long groupId,
                                             @PathVariable Long userId,
                                             @RequestHeader("User-Id") Long requesterId) {
        groupService.removeMember(groupId, userId, requesterId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/messages/{messageId}/pin")
    public ResponseEntity<Void> pinMessage(@PathVariable Long messageId,
                                           @RequestHeader("User-Id") Long requesterId) {
        groupService.pinMessage(messageId, requesterId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{groupId}")
    public ResponseEntity<Void> deleteGroup(@PathVariable Long groupId,
                                            @RequestHeader("User-Id") Long requesterId) {
        groupService.deleteGroup(groupId, requesterId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{groupId}/members")
    public ResponseEntity<Page<GroupResponse>> listMembers(@PathVariable Long groupId,
                                                           @RequestParam(defaultValue = "0") int page,
                                                           @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(groupService.listMembers(groupId, pageable));
    }

    @PutMapping("/{groupId}")
    public ResponseEntity<StudyGroup> editGroup(@PathVariable Long groupId,
                                                @RequestBody StudyGroup group,
                                                @RequestHeader("User-Id") Long requesterId) {
        return ResponseEntity.ok(groupService.editGroup(groupId, group, requesterId));
    }

    @GetMapping("/{groupId}/pinned-messages")
    public ResponseEntity<Page<Message>> getPinnedMessages(@PathVariable Long groupId,
                                                           @RequestParam(defaultValue = "0") int page,
                                                           @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(groupService.getPinnedMessages(groupId, pageable));
    }

    @PostMapping("/{groupId}/documents")
    public ResponseEntity<Message> shareDocument(@PathVariable Long groupId,
                                                 @RequestHeader("User-Id") Long userId,
                                                 @RequestBody Map<String, String> requestBody) throws Exception {
        String documentId = requestBody.get("documentId");
        String shareUrl = requestBody.get("shareUrl");
        if (documentId == null || shareUrl == null) {
            throw new IllegalArgumentException("documentId and shareUrl are required");
        }
        return ResponseEntity.ok(groupService.shareDocumentToGroup(groupId, userId, documentId, shareUrl));
    }
}