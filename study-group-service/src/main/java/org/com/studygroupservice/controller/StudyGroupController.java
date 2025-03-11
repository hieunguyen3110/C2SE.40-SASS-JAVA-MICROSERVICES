package org.com.studygroupservice.controller;

import lombok.RequiredArgsConstructor;
import org.com.studygroupservice.entity.Message;
import org.com.studygroupservice.entity.StudyGroup;
import org.com.studygroupservice.service.StudyGroupService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

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

    @PostMapping("/{groupId}/messages")
    public ResponseEntity<Message> sendMessage(@PathVariable Long groupId,
                                               @RequestHeader("User-Id") Long userId,
                                               @RequestBody String content) {
        return ResponseEntity.ok(groupService.sendMessage(groupId, userId, content));
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
    public ResponseEntity<?> listMembers(@PathVariable Long groupId) {
        return ResponseEntity.ok(groupService.listMembers(groupId));
    }

    @PutMapping("/{groupId}")
    public ResponseEntity<StudyGroup> editGroup(@PathVariable Long groupId,
                                                @RequestBody StudyGroup group,
                                                @RequestHeader("User-Id") Long requesterId) {
        return ResponseEntity.ok(groupService.editGroup(groupId, group, requesterId));
    }

    @GetMapping("/{groupId}/pinned-messages")
    public ResponseEntity<List<Message>> getPinnedMessages(@PathVariable Long groupId) {
        return ResponseEntity.ok(groupService.getPinnedMessages(groupId));
    }

    @PostMapping("/{groupId}/documents")
    public ResponseEntity<Message> shareDocument(@PathVariable Long groupId,
                                                 @RequestHeader("User-Id") Long userId,
                                                 @RequestBody String documentId) {
        return ResponseEntity.ok(groupService.shareDocument(groupId, userId, documentId));
    }
}