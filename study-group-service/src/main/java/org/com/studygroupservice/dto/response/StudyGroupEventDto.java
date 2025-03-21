package org.com.studygroupservice.dto.response;

import lombok.*;

@Data
@NoArgsConstructor
public class StudyGroupEventDto {
    private Long groupId;
    private Long userId;
    private String message;
    private String isPrivate;

    public StudyGroupEventDto(Long groupId, Long userId, String message, String isPrivate) {
        this.groupId = groupId;
        this.userId = userId;
        this.message = message;
        this.isPrivate = isPrivate;
    }
}
