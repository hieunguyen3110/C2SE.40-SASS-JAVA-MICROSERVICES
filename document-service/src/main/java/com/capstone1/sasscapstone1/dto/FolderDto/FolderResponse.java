package com.capstone1.sasscapstone1.dto.FolderDto;

import com.capstone1.sasscapstone1.dto.DocumentDto.DocumentDto;
import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FolderResponse {
    private long folderId;
    private long accountId;
    private String folderName;
    private String authorName;
    private long numberDoc;
    List<DocumentDto> documents;
    private String description;
}
