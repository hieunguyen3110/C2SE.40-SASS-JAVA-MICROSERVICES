package org.com.notificationservice.dto.response;

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
}
