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
    public ResponseEntity<StudyGroup> createGroup(@RequestBody Map<String, Object> requestBody) {
        Long userId = Long.valueOf(requestBody.get("userId").toString());
        StudyGroup group = new StudyGroup();
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
                                             @RequestBody Map<String, Object> requestBody) {
        Long requesterId = Long.valueOf(requestBody.get("requesterId").toString());
        groupService.removeMember(groupId, userId, requesterId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/messages/{messageId}/pin")
    public ResponseEntity<Void> pinMessage(@PathVariable Long messageId,
                                           @RequestBody Map<String, Object> requestBody) {
        Long requesterId = Long.valueOf(requestBody.get("requesterId").toString());
        groupService.pinMessage(messageId, requesterId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{groupId}")
    public ResponseEntity<Void> deleteGroup(@PathVariable Long groupId,
                                            @RequestBody Map<String, Object> requestBody) {
        Long requesterId = Long.valueOf(requestBody.get("requesterId").toString());
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
                                                @RequestBody Map<String, Object> requestBody) {
        Long requesterId = Long.valueOf(requestBody.get("requesterId").toString());
        StudyGroup group = new StudyGroup();
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
                                                 @RequestBody Map<String, Object> requestBody) throws Exception {
        Long userId = Long.valueOf(requestBody.get("userId").toString());
        String documentId = requestBody.get("documentId").toString();
        String shareUrl = requestBody.get("shareUrl").toString();
        return ResponseEntity.ok(groupService.shareDocumentToGroup(groupId, userId, documentId, shareUrl));
    }
}