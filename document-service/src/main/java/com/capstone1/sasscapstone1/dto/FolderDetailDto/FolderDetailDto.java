package com.capstone1.sasscapstone1.dto.FolderDetailDto;

import com.capstone1.sasscapstone1.dto.DocumentDto.DocumentDto;
import lombok.Data;

import java.util.List;

@Data
public class FolderDetailDto {
    private String folderName;
    private String subjectCode;
    private long totalDocuments;
    private String authorName;
    private List<DocumentDto> documents;
}
