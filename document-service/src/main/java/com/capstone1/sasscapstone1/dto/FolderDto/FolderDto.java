package com.capstone1.sasscapstone1.dto.FolderDto;

import lombok.Data;

@Data
public class FolderDto {
    private Long folderId;
    private String folderName;
    private String description;
    private String accountEmail;
    private long documentCount;
    private String subjectCode;
    private String ownerName;
}
