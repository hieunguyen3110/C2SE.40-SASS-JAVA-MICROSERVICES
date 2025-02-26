package com.capstone1.sasscapstone1.dto.AdminDocumentDto;

import com.capstone1.sasscapstone1.dto.FolderDto.FolderDto;
import com.capstone1.sasscapstone1.entity.Folder;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminDocumentDto {
    private Long docId;
    private String title;
    private String description;
    private Boolean isActive;
    private String type;
    private String subjectName;
    private String facultyName;
    private Long folderId;
}
