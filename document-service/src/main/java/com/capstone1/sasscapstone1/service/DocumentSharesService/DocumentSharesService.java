package com.capstone1.sasscapstone1.service.DocumentSharesService;

import com.capstone1.sasscapstone1.dto.response.ApiResponse;
import com.capstone1.sasscapstone1.entity.DocumentShares;

import java.util.List;

public interface DocumentSharesService {
    ApiResponse<DocumentShares> shareDocument(Long documentId, Long folderId, String email, String shareUrl) throws Exception;
    ApiResponse<List<DocumentShares>> getSharesByEmail(String email) throws Exception;
    ApiResponse<List<DocumentShares>> getSharesByDocument(Long documentId) throws Exception;
}
