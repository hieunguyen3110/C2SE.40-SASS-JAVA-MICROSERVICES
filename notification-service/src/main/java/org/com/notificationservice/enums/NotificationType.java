package org.com.notificationservice.enums;

import lombok.Getter;

@Getter
public enum NotificationType {
    UPLOAD_FILE("UPLOAD FILE"),
    FOLLOW("FOLLOW");
    NotificationType(String name) {
        this.name = name;
    }

    private final String name;
}
