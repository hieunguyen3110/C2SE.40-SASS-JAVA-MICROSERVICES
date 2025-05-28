package com.capstone1.sasscapstone1.request;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckFileRequest {
    private String filePath;
}
