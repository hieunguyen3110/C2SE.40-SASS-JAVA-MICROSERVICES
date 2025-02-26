package com.capstone1.sasscapstone1.dto.DocumentDetailDto;

import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentDetailDto {
    private Long docId;
    private String title;
    private String folderName;
    private String subjectName;
    private String facultyName;
    private LocalDateTime createdAt;
    private String authorName;
    private String filePath;
    private String profilePicture;
}
