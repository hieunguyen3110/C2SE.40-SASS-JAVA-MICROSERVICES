package com.capstone1.sasscapstone1.enums;

import lombok.Getter;

@Getter
public enum KafkaTopic {
    UPLOAD_FILE("file-upload-topic"),
    FOLLOW("follow-topic");
    KafkaTopic(String name) {
        this.name = name;
    }

    private final String name;
}
