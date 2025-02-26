package com.capstone1.sasscapstone1.dto.DocumentDto;

import com.capstone1.sasscapstone1.entity.Folder;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentDto {
    private Long docId;
    private String title;
    private String description;
    private String type;
    private Boolean isActive;
    private String subjectName;
    private String filePath;
    private Long accountId;
    private Long facultyId;
    private String facultyName;
    private String subjectCode;
    private Folder folder;
    private String authorName;
    private String authorProfilePicture;
    private Long downloadCount;
}
