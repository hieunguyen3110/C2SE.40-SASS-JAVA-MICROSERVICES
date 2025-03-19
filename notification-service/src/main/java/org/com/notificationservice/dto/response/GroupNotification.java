package org.com.notificationservice.dto.response;

import lombok.Data;

@Data
public class GroupNotification {
    private Long groupId;
    private Long userId;
    private String action;
    private Long messageId;
    private Object data;
    private Long timestamp;
}
