package com.capstone1.sasscapstone1.controller.AdminDashboardController;

import com.capstone1.sasscapstone1.dto.AccountDto.AccountDto;
import com.capstone1.sasscapstone1.dto.AdminDashboardStatsDto.StatsDto;
import com.capstone1.sasscapstone1.dto.AdminDocumentDto.AdminDocumentDto;
import com.capstone1.sasscapstone1.dto.DocumentListDto.DocumentListDto;
import com.capstone1.sasscapstone1.dto.response.ApiResponse;
import com.capstone1.sasscapstone1.enums.ErrorCode;
import com.capstone1.sasscapstone1.exception.ApiException;
import com.capstone1.sasscapstone1.request.TrainDocumentRequest;
import com.capstone1.sasscapstone1.service.AdminDashboardService.AdminDashboardService;
import com.capstone1.sasscapstone1.service.DocumentCheckService.DocumentCheckService;
import com.capstone1.sasscapstone1.service.DocumentManagementService.DocumentManagementService;
import com.capstone1.sasscapstone1.service.DocumentService.DocumentService;
import com.capstone1.sasscapstone1.util.CreateApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/dashboard")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final AdminDashboardService dashboardService;
    private final DocumentManagementService documentManagementService;
    private final DocumentCheckService documentCheckService;
    private final DocumentService documentService;

    @GetMapping("/stats")
    public ApiResponse<StatsDto> getDashboardStats() {
        StatsDto stats = dashboardService.getDashboardStats();
        return CreateApiResponse.createResponse(stats,false);
    }

    @GetMapping("/documents")
    public ApiResponse<Page<DocumentListDto>> listDocuments(@RequestParam(defaultValue = "0") int page,
                                                               @RequestParam(defaultValue = "10") int size) {
        return CreateApiResponse.createResponse(documentManagementService.listDocuments(page, size),false);
    }

    @GetMapping("/documents/search")
    public ApiResponse<Page<DocumentListDto>> searchDocuments(@RequestParam String keyword,
                                                                 @RequestParam(defaultValue = "0") int page,
                                                                 @RequestParam(defaultValue = "10") int size) {
        return CreateApiResponse.createResponse(documentManagementService.searchDocuments(keyword, page, size),false);
    }

    @DeleteMapping("/documents")
    public ApiResponse<String> softDeleteDocuments(@RequestBody List<Long> docIds) {
        documentManagementService.softDeleteDocuments(docIds);
        return CreateApiResponse.createResponse("Documents deleted successfully.",false);
    }

    @PostMapping("/documents/approve")
    public ApiResponse<?> approveDocuments(@RequestBody List<Long> docIds) {
        // Lấy thông tin đăng nhập từ SecurityContext
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof AnonymousAuthenticationToken) {
            throw new ApiException(ErrorCode.FORBIDDEN.getStatusCode().value(),"You are not authorized to perform this action.");
        }

        // Lấy email của admin từ thông tin đăng nhập
        AccountDto account = (AccountDto) authentication.getPrincipal();
        String adminApprove = account.getFirstName() + " " + account.getLastName();

        try {
            // Gọi service để duyệt danh sách tài liệu và lấy tên admin
            documentManagementService.approveDocuments(docIds, adminApprove);
            return CreateApiResponse.createResponse("Documents approved successfully.",false);
        } catch (ApiException ex) {
            throw new ApiException(ex.getCode(),ex.getMessage());
        } catch (Exception e) {
            throw new ApiException(ErrorCode.INTERNAL_SERVER_ERROR.getStatusCode().value(),"An unexpected error occurred: " + e.getMessage());
        }
    }


    @PostMapping("/check-document")
    public ResponseEntity<String> checkDocument(@RequestParam Long docId) {
        try {
            Authentication authentication= SecurityContextHolder.getContext().getAuthentication();
            if(!(authentication instanceof AnonymousAuthenticationToken)){
                documentCheckService.checkDocument(docId);
                return ResponseEntity.ok("Document checked successfully.");
            }else{
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You must login to access");
            }

        } catch (ApiException e) {
            throw new ApiException(e.getCode(),e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error checking document: " + e.getMessage());
        }
    }

    @GetMapping("/documents/{docId}")
    public ResponseEntity<?> getDocumentDetails(@PathVariable Long docId) {
        return ResponseEntity.ok(documentManagementService.getDocumentDetails(docId));
    }

    @PutMapping("/documents/{docId}")
    public ResponseEntity<?> updateDocument(@PathVariable Long docId,
                                                 @RequestBody AdminDocumentDto documentDto) {
        documentManagementService.updateDocument(docId, documentDto);
        return ResponseEntity.ok("Document updated successfully.");
    }
    @PostMapping("/train-document")
    public ApiResponse<String> trainDocument(@RequestBody TrainDocumentRequest request) throws Exception {
       return documentService.trainDocument(request);
    }
}
