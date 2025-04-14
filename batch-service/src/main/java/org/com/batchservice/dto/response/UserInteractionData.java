package org.com.batchservice.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserInteractionData {
    private Long account_id;
    private Long document_id;
    private LocalDateTime timestamp;
    private double view_time;
    private int rating;
}
