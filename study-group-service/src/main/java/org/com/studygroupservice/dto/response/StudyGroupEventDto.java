package org.com.studygroupservice.dto.response;

import lombok.*;
import org.com.studygroupservice.entity.JoinRequest;

import java.util.List;

@Data
@NoArgsConstructor
public class StudyGroupEventDto {
    private Long groupId;
    private Long userId;
    private String message;
    private Boolean isPrivate;
    private String groupName;
    private String description;
    private String subjectName;
    private String picture;
    private int memberLimited;
    private int memberCount;
    private List<JoinRequestDto> joinRequests;

    public StudyGroupEventDto(Long groupId, Long userId, String message, Boolean isPrivate) {
        this.groupId = groupId;
        this.userId = userId;
        this.message = message;
        this.isPrivate = isPrivate;
    }

    public StudyGroupEventDto(Long groupId, Long userId, Boolean isPrivate, String groupName, String description, String subjectName, String picture, int memberLimited, List<JoinRequestDto> joinRequests, int memberCount) {
        this.groupId = groupId;
        this.userId = userId;
        this.isPrivate = isPrivate;
        this.groupName = groupName;
        this.description = description;
        this.subjectName = subjectName;
        this.picture = picture;
        this.memberLimited = memberLimited;
        this.joinRequests = joinRequests;
    }
}
