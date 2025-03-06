package org.com.notificationservice.dto.request;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationEventRequest {
    private long accountId;
    private String message;
    private String type;
}
