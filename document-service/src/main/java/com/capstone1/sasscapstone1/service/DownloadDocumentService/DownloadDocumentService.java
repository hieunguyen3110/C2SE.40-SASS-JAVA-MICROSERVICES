package com.capstone1.sasscapstone1.service.DownloadDocumentService;

import com.capstone1.sasscapstone1.dto.response.ApiResponse;

import java.io.IOException;

public interface DownloadDocumentService {
    ApiResponse<String> downloadDocument(Long documentId, String username) throws Exception;
}
