package org.com.websocketserver.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MessageRequest {
    private Long groupId;
    private String content;
    private String timestamp;
    private String username;
    private String profilePicture;
    private String documentId;
    private String documentName;
    private String docFilePath;
    private String messageType;
}
