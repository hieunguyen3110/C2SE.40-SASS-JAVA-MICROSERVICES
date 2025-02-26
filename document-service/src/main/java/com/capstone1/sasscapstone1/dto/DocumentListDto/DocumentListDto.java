package com.capstone1.sasscapstone1.dto.DocumentListDto;

import lombok.Data;

@Data
public class DocumentListDto {
    private Long docId;
    private String title;
    private String subjectName;
    private String folderName;
    private String authorName;
    private String createdAt;
    private String approvedBy;
    private String filePath;
    private String fileName;
    private Boolean isActive;
    private Boolean isCheck;
    private Boolean isTrain;
}
