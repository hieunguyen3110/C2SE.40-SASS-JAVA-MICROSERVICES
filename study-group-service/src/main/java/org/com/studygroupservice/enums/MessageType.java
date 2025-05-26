package org.com.studygroupservice.enums;

import lombok.Getter;

@Getter
public enum MessageType {
    NOTIFICATION("NOTIFICATION"),
    MESSAGE("MESSAGE"),
    DOCUMENT("DOCUMENT");

    MessageType(String messageType) {
        this.messageType = messageType;
    }

    private final String messageType;

}
