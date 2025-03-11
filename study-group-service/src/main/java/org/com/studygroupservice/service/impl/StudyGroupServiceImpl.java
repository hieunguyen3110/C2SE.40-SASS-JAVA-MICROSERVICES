package org.com.studygroupservice.service.impl;

import lombok.RequiredArgsConstructor;
import org.com.studygroupservice.entity.Message;
import org.com.studygroupservice.dto.response.AccountDto;
import org.com.studygroupservice.dto.response.GroupResponse;
import org.com.studygroupservice.entity.GroupMember;
import org.com.studygroupservice.entity.StudyGroup;
import org.com.studygroupservice.exception.ApiException;
import org.com.studygroupservice.repository.GroupMemberRepository;
import org.com.studygroupservice.repository.MessageRepository;
import org.com.studygroupservice.repository.StudyGroupRepository;
import org.com.studygroupservice.service.StudyGroupService;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StudyGroupServiceImpl implements StudyGroupService {

    private final StudyGroupRepository groupRepository;
    private final GroupMemberRepository memberRepository;
    private final MessageRepository messageRepository;
    private final WebClient.Builder webClientBuilder;

    @Override
    public StudyGroup createGroup(StudyGroup group, Long ownerId) {
        try {
            group.setOwnerId(ownerId);
            StudyGroup savedGroup = groupRepository.save(group);
            addMember(savedGroup.getId(), ownerId);
            return savedGroup;
        } catch (Exception e) {
            throw new ApiException(500, "Failed to create group: " + e.getMessage());
        }
    }

    @Override
    public StudyGroup getGroupDetails(Long groupId) {
        try {
            return groupRepository.findById(groupId)
                    .orElseThrow(() -> new ApiException(404, "Group not found"));
        } catch (Exception e) {
            throw new ApiException(500, "Failed to fetch group details: " + e.getMessage());
        }
    }

    @Override
    public Message sendMessage(Long groupId, Long senderId, String content) {
        try {
            StudyGroup group = getGroupDetails(groupId);
            Message message = new Message();
            message.setSenderId(senderId);
            message.setContent(content);
            message.setPinned(false);
            message.setGroup(group);
            return messageRepository.save(message);
        } catch (Exception e) {
            throw new ApiException(500, "Failed to send message: " + e.getMessage());
        }
    }

    @Override
    public void removeMember(Long groupId, Long userId, Long requesterId) {
        try {
            StudyGroup group = getGroupDetails(groupId);
            if (!group.getOwnerId().equals(requesterId)) {
                throw new ApiException(403, "Only owner can remove members");
            }
            GroupMember member = memberRepository.findByStudyGroupId(groupId).stream()
                    .filter(m -> m.getAccountId().equals(userId))
                    .findFirst()
                    .orElseThrow(() -> new ApiException(404, "Member not found"));
            memberRepository.delete(member);
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException(500, "Failed to remove member: " + e.getMessage());
        }
    }

    @Override
    public void pinMessage(Long messageId, Long requesterId) {
        try {
            Message message = messageRepository.findById(messageId)
                    .orElseThrow(() -> new ApiException(404, "Message not found"));
            StudyGroup group = message.getGroup();
            if (!group.getOwnerId().equals(requesterId)) {
                throw new ApiException(403, "Only owner can pin messages");
            }
            message.setPinned(true);
            messageRepository.save(message);
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException(500, "Failed to pin message: " + e.getMessage());
        }
    }

    @Override
    public void deleteGroup(Long groupId, Long requesterId) {
        try {
            StudyGroup group = getGroupDetails(groupId);
            if (!group.getOwnerId().equals(requesterId)) {
                throw new ApiException(403, "Only owner can delete group");
            }
            groupRepository.delete(group);
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException(500, "Failed to delete group: " + e.getMessage());
        }
    }

    @Override
    public List<GroupResponse> listMembers(Long groupId) {
        try {
            List<GroupMember> members = memberRepository.findByStudyGroupId(groupId);
            if (members.isEmpty()) {
                throw new ApiException(404, "No members found in group");
            }
            WebClient webClient = webClientBuilder.baseUrl("http://identity-service").build();

            return members.stream().map(member -> {
                String url = "/admin/account/users/" + member.getAccountId();
                try {
                    AccountDto account = webClient.get()
                            .uri(url)
                            .retrieve()
                            .bodyToMono(AccountDto.class)
                            .block();

                    if (account == null) {
                        throw new ApiException(404, "Account not found for member ID: " + member.getAccountId());
                    }

                    return new GroupResponse(member.getId(), account.getUsername(), account.getEmail());
                } catch (Exception e) {
                    throw new ApiException(500, "Error fetching account details for member ID " + member.getAccountId() + ": " + e.getMessage());
                }
            }).collect(Collectors.toList());
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException(500, "Failed to list members: " + e.getMessage());
        }
    }

    @Override
    public StudyGroup editGroup(Long groupId, StudyGroup updatedGroup, Long requesterId) {
        try {
            StudyGroup group = getGroupDetails(groupId);
            if (!group.getOwnerId().equals(requesterId)) {
                throw new ApiException(403, "Only owner can edit group");
            }
            group.setName(updatedGroup.getName());
            group.setDescription(updatedGroup.getDescription());
            return groupRepository.save(group);
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException(500, "Failed to edit group: " + e.getMessage());
        }
    }

    @Override
    public List<Message> getPinnedMessages(Long groupId) {
        try {
            List<Message> pinnedMessages = messageRepository.findByGroupIdAndIsPinnedTrue(groupId);
            if (pinnedMessages.isEmpty()) {
                throw new ApiException(404, "No pinned messages found in group");
            }
            return pinnedMessages;
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException(500, "Failed to fetch pinned messages: " + e.getMessage());
        }
    }

    @Override
    public Message shareDocument(Long groupId, Long senderId, String documentId) {
        try {
            String content = "Shared document: " + documentId;
            return sendMessage(groupId, senderId, content);
        } catch (Exception e) {
            throw new ApiException(500, "Failed to share document: " + e.getMessage());
        }
    }

    private void addMember(Long groupId, Long userId) {
        try {
            StudyGroup group = getGroupDetails(groupId);
            GroupMember member = new GroupMember(null, userId, group);
            memberRepository.save(member);
        } catch (Exception e) {
            throw new ApiException(500, "Failed to add member: " + e.getMessage());
        }
    }
}