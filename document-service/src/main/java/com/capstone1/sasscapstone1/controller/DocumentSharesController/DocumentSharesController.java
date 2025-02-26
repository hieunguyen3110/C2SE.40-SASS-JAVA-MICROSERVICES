package com.capstone1.sasscapstone1.controller.DocumentSharesController;

import com.capstone1.sasscapstone1.dto.response.ApiResponse;
import com.capstone1.sasscapstone1.entity.DocumentShares;
import com.capstone1.sasscapstone1.service.DocumentSharesService.DocumentSharesService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/shares")
@RequiredArgsConstructor
public class DocumentSharesController {
    private final DocumentSharesService documentShareService;

    @PostMapping("/email")
    public ApiResponse<DocumentShares> shareDocument(@RequestParam Long documentId,
                                                     @RequestParam(required = false) Long folderId,
                                                     @RequestParam String email,
                                                     @RequestParam String shareUrl) throws Exception {
        return documentShareService.shareDocument(documentId, folderId, email, shareUrl);
    }

    @GetMapping("/email/{email}")
    public ApiResponse<List<DocumentShares>> getSharesByEmail(@PathVariable String email) throws Exception {
        return documentShareService.getSharesByEmail(email);
    }

    @GetMapping("/document/{documentId}")
    public ApiResponse<List<DocumentShares>> getSharesByDocument(@PathVariable Long documentId) throws Exception {
        return documentShareService.getSharesByDocument(documentId);
    }
}
