package com.capstone1.sasscapstone1.dto.PopularDocumentDto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PopularDocumentDto {
    private Long docId;
    private String title;
    private String description;
    private String filePath;
    private String subject;
    private String facultyName;
    private String authorName;
    private Integer downloadCount;
    private String profilePicture;
}
