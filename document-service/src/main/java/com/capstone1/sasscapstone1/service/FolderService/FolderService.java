package com.capstone1.sasscapstone1.service.FolderService;

import com.capstone1.sasscapstone1.dto.AccountDto.AccountDto;
import com.capstone1.sasscapstone1.dto.FolderDownloadStatsDto.FolderDownloadStatsDto;
import com.capstone1.sasscapstone1.dto.FolderDto.FolderDto;
import com.capstone1.sasscapstone1.dto.FolderDto.FolderResponse;
import com.capstone1.sasscapstone1.dto.response.ApiResponse;
import org.springframework.data.domain.Page;

import java.util.List;

public interface FolderService {
    FolderDto createFolder(String folderName, String description, AccountDto account);
    FolderDto updateFolder(Long folderId, String folderName, String description);
    void deleteFolder(Long folderId);
    List<FolderDto> getAllFolders(AccountDto account);
    ApiResponse<FolderResponse> getFolderById(String email, Long folderId);
    ApiResponse<FolderResponse> getFolderById(Long folderId);
    Page<FolderDownloadStatsDto> getTopFoldersByDownloadCount(int page, int size);
}
