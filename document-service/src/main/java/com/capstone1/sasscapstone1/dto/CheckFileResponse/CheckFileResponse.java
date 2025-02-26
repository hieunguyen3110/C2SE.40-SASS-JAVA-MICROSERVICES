package com.capstone1.sasscapstone1.dto.CheckFileResponse;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CheckFileResponse {
    private boolean containsSensitiveWords;
    private List<String> sensitiveWords;
}
