package org.com.batchservice.dto.response;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageAnalyze {
    private Long accountId;
    private String message;
}
