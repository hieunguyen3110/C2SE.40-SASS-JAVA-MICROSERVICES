package org.com.batchservice.dto.request;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UploadFileRequest {
    private String fileName;
    private String filePath;
    private Long docId;
}
