package org.com.batchservice.dto.response;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentDto {
    private Long docId;
    private String fileName;
    private String description;
    private String filePath;
    private String subjectCode;
    private Long subjectId;
    private String subjectName;
}
