package org.com.studygroupservice.dto.response;

import lombok.Data;

@Data
public class SubjectDto {
    private Long subjectId;
    private String subjectCode;
    private String subjectName;

    public SubjectDto(Long subjectId, String subjectCode, String subjectName) {
        this.subjectId = subjectId;
        this.subjectCode = subjectCode;
        this.subjectName = subjectName;
    }
}
