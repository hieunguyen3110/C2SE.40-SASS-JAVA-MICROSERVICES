package org.com.studygroupservice.dto.request;

import lombok.Data;

@Data
public class ChatMessage {
    private Long groupId;
    private Long senderId;
    private String content;
}
