package org.com.studygroupservice.dto.request;

import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.com.studygroupservice.enums.MessageType;

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
