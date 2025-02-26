package com.capstone1.sasscapstone1.dto.SubjectDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class SubjectDto {
    private String subjectCode;
    private String subjectName;
}
