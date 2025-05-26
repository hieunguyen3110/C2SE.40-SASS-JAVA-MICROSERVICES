package org.com.studygroupservice.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageResponse {
    private Long senderId;
    private Long groupId;
    private String content;
    private String timestamp;
    private String username;
    private String profilePicture;
    private Long messageId;
    private String documentId;
    private String documentName;
    private String docFilePath;
    private String messageType;
    private LocalDateTime createdAt;
}
