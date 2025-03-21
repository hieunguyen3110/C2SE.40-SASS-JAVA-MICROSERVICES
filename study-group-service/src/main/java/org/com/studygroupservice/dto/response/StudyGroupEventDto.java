package org.com.studygroupservice.dto.response;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class StudyGroupEventDto {
    private Long groupId;
    private Long userId;
    private String message;
    private String isPrivate;

    public StudyGroupEventDto(Long groupId, Long userId, String message, String isPrivate) {
    }
}
