package com.capstone1.sasscapstone1.dto.FolderDownloadStatsDto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
@AllArgsConstructor
public class FolderDownloadStatsDto {
    private Long folderId;
    private String folderName;
    private Long downloadCount;
    private String ownerName;
}
