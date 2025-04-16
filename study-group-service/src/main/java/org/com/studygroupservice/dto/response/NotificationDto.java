package org.com.studygroupservice.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDto {
    private Long notificationId;
    private Long accountId;
    private String message;
    private String type;
    private Boolean isRead;
    private Boolean isSaved;
    private Boolean deletedFlag;
    private LocalDateTime createdAt;

    public NotificationDto(Long groupId, long accountId, String content) {
        this.notificationId = groupId;
        this.accountId = accountId;
        this.message = content;
    }
}
