package com.capstone1.sasscapstone1.dto.NotificationDto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
public class NotificationDto {
    private Long notificationId;
    private String message;
    private String type;
    private Boolean isRead;
    private LocalDateTime createdAt;

    @JsonCreator
    public NotificationDto(
            @JsonProperty("notificationId") Long notificationId,
            @JsonProperty("message") String message,
            @JsonProperty("type") String type,
            @JsonProperty("isRead") Boolean isRead,
            @JsonProperty("createdAt") LocalDateTime createdAt) {
        this.notificationId = notificationId;
        this.message = message;
        this.type = type;
        this.isRead = isRead;
        this.createdAt = createdAt;
    }
}
