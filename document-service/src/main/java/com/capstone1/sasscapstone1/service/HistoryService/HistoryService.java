package com.capstone1.sasscapstone1.service.HistoryService;

import com.capstone1.sasscapstone1.dto.FolderDownloadStatsDto.FolderDownloadStatsDto;
import com.capstone1.sasscapstone1.dto.PopularDocumentDto.PopularDocumentDto;
import org.springframework.data.domain.Page;

import java.util.List;

public interface HistoryService {
    void trackDownload(Long docId, String username);


}
