package org.com.batchservice.dto.response;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckFileResponse {
    private boolean containsSensitiveWords;
    private List<String> sensitiveWords;
}
