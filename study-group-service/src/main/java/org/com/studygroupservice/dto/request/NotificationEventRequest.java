package org.com.studygroupservice.dto.request;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationEventRequest {
    private long accountId;
    private String message;
    private String type;
}
