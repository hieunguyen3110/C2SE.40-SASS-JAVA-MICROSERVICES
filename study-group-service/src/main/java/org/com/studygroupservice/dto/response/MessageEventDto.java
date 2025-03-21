package org.com.studygroupservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class MessageEventDto {
    private Long groupId;
    private Long senderId;
    private String eventType;
    private Long messageId;
    private String content;

    public MessageEventDto(Long groupId, Long senderId, String eventType, Long messageId, String content) {
        this.groupId = groupId;
        this.senderId = senderId;
        this.content = content;
        this.messageId = messageId;
        this.eventType = eventType;
    }
}
