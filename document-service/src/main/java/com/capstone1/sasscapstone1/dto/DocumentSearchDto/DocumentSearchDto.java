package com.capstone1.sasscapstone1.dto.DocumentSearchDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentSearchDto {
    private long docId;
    private String title;
    private String description;
    private String content;
    private String type;
    private String subjectName;
    private String facultyName;
}