package org.com.studygroupservice.dto.response;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageResponse {
    private Long groupId;
    private String content;
    private String timestamp;
    private String username;
    private String profilePicture;
    private Long messageId;
}
