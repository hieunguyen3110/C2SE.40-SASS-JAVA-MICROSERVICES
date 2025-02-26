package com.capstone1.sasscapstone1.controller.DownloadDocumentController;

import com.capstone1.sasscapstone1.dto.response.ApiResponse;
import com.capstone1.sasscapstone1.service.DownloadDocumentService.DownloadDocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@RestController
@RequestMapping("/download")
@RequiredArgsConstructor
public class DownloadDocumentController {

    private final DownloadDocumentService downloadDocumentService;

    // API tải tài liệu và ghi nhận lượt tải
    @GetMapping("/{documentId}")
    public ApiResponse<String> downloadDocument(@PathVariable Long documentId) throws Exception {
        // Lấy username từ thông tin người dùng hiện tại
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        return downloadDocumentService.downloadDocument(documentId, username);
    }
}
