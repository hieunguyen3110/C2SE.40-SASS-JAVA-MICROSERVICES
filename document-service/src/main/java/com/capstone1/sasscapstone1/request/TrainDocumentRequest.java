package com.capstone1.sasscapstone1.request;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TrainDocumentRequest {
    private String fileName;
    private String filePath;
}
