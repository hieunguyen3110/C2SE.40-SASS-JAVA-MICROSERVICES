package com.capstone1.sasscapstone1.controller.SavedDocumentsController;

import com.capstone1.sasscapstone1.dto.SavedDocumentsDto.SavedDocumentsDto;
import com.capstone1.sasscapstone1.dto.response.ApiResponse;
import com.capstone1.sasscapstone1.enums.ErrorCode;
import com.capstone1.sasscapstone1.exception.ApiException;
import com.capstone1.sasscapstone1.service.SavedDocumentsService.SavedDocumentsService;
import com.capstone1.sasscapstone1.util.CreateApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/saved-documents")
public class SavedDocumentsController {

    private final SavedDocumentsService savedDocumentsService;

    @Autowired
    public SavedDocumentsController(SavedDocumentsService savedDocumentsService) {
        this.savedDocumentsService = savedDocumentsService;
    }

    // API lưu tài liệu
    @PostMapping("/save")
    public ApiResponse<String> saveDocument(@RequestParam Long docId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            Long accountId = getAccountIdFromAuthentication(authentication);

            savedDocumentsService.saveDocument(docId, accountId);
            return CreateApiResponse.createResponse("Document saved successfully.",false);
        } else {
            throw new ApiException(ErrorCode.FORBIDDEN.getStatusCode().value(),"You are not authorized to perform this action.");
        }
    }

    // API hiển thị danh sách tài liệu đã lưu
    @GetMapping("/list")
    public ApiResponse<Page<SavedDocumentsDto>> listSavedDocuments(@RequestParam int page, @RequestParam int size) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            Long accountId = getAccountIdFromAuthentication(authentication);

            Page<SavedDocumentsDto> savedDocuments = savedDocumentsService.listSavedDocuments(accountId, page, size);
            return CreateApiResponse.createResponse(savedDocuments,false);
        } else {
            throw new ApiException(ErrorCode.FORBIDDEN.getStatusCode().value(),"You are not authorized to perform this action.");
        }
    }

    // API xóa tài liệu đã lưu
    @DeleteMapping("/delete")
    public ApiResponse<String> deleteSavedDocument(@RequestParam Long docId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            Long accountId = getAccountIdFromAuthentication(authentication);

            savedDocumentsService.deleteSavedDocument(docId, accountId);
            return CreateApiResponse.createResponse("Document removed successfully from saved list.",false);
        } else {
            throw new ApiException(ErrorCode.FORBIDDEN.getStatusCode().value(),"You are not authorized to perform this action.");
        }
    }

    // Helper method để lấy accountId từ Authentication
    private Long getAccountIdFromAuthentication(Authentication authentication) {
        // Trích xuất accountId từ principal (nếu principal là kiểu Account)
        return ((com.capstone1.sasscapstone1.entity.Account) authentication.getPrincipal()).getAccountId();
    }
}
