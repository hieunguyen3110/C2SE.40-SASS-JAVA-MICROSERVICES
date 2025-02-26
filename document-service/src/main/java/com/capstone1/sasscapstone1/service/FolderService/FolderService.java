package com.capstone1.sasscapstone1.service.FolderService;

import com.capstone1.sasscapstone1.dto.FolderDownloadStatsDto.FolderDownloadStatsDto;
import com.capstone1.sasscapstone1.dto.FolderDto.FolderDto;
import com.capstone1.sasscapstone1.dto.FolderDto.FolderResponse;
import com.capstone1.sasscapstone1.dto.response.ApiResponse;
import com.capstone1.sasscapstone1.entity.Account;
import org.springframework.data.domain.Page;

import java.util.List;

public interface FolderService {
    FolderDto createFolder(String folderName, String description, Account account);
    FolderDto updateFolder(Long folderId, String folderName, String description);
    void deleteFolder(Long folderId);
    List<FolderDto> getAllFolders(Account account);
    ApiResponse<FolderResponse> getFolderById(Long accountId, Long folderId);
    ApiResponse<FolderResponse> getFolderById(Long folderId);
    Page<FolderDownloadStatsDto> getTopFoldersByDownloadCount(int page, int size);
}
