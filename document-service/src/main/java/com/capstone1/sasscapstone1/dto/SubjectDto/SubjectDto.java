package com.capstone1.sasscapstone1.dto.SubjectDto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubjectDto {
    private Long subjectId;
    private String subjectCode;
    private String subjectName;
}
