package com.capstone1.sasscapstone1.request;

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
