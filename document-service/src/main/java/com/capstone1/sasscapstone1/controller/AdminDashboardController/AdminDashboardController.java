package com.capstone1.sasscapstone1.controller.AdminDashboardController;

import com.capstone1.sasscapstone1.dto.AdminDashboardStatsDto.StatsDto;
import com.capstone1.sasscapstone1.dto.AdminDocumentDto.AdminDocumentDto;
import com.capstone1.sasscapstone1.dto.DocumentListDto.DocumentListDto;
import com.capstone1.sasscapstone1.dto.UserListDto.UserListDto;
import com.capstone1.sasscapstone1.dto.UserProfileResponseDTO.UserProfileResponse;
import com.capstone1.sasscapstone1.dto.response.ApiResponse;
import com.capstone1.sasscapstone1.entity.Account;
import com.capstone1.sasscapstone1.enums.ErrorCode;
import com.capstone1.sasscapstone1.exception.ApiException;
import com.capstone1.sasscapstone1.request.TrainDocumentRequest;
import com.capstone1.sasscapstone1.service.AdminDashboardService.AdminDashboardService;
import com.capstone1.sasscapstone1.service.AdminUserManagementService.AdminUserManagementService;
import com.capstone1.sasscapstone1.service.DocumentCheckService.DocumentCheckService;
import com.capstone1.sasscapstone1.service.DocumentManagementService.DocumentManagementService;
import com.capstone1.sasscapstone1.service.DocumentService.DocumentService;
import com.capstone1.sasscapstone1.util.CreateApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
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
public class AdminDashboardController {

    private final AdminDashboardService dashboardService;
    private final AdminUserManagementService userManagementService;
    private final DocumentManagementService documentManagementService;
    private final DocumentCheckService documentCheckService;
    private final DocumentService documentService;
    private final AdminUserManagementService adminUserManagementService;


    @Autowired
    public AdminDashboardController(AdminDashboardService dashboardService, AdminUserManagementService userManagementService,
                                    DocumentManagementService documentManagementService, DocumentCheckService documentCheckService, DocumentService documentService, AdminUserManagementService adminUserManagementService) {
        this.dashboardService = dashboardService;
        this.userManagementService = userManagementService;
        this.documentManagementService = documentManagementService;
        this.documentCheckService = documentCheckService;
        this.documentService= documentService;
        this.adminUserManagementService = adminUserManagementService;
    }

    @GetMapping("/stats")
    public ApiResponse<StatsDto> getDashboardStats() {
        StatsDto stats = dashboardService.getDashboardStats();
        return CreateApiResponse.createResponse(stats,false);
    }

    // List users
    @GetMapping("/users")
    public ApiResponse<Page<UserListDto>> listUsers(@RequestParam(defaultValue = "0") int page,
                                                       @RequestParam(defaultValue = "10") int size) {
        return CreateApiResponse.createResponse(userManagementService.listUsers(page, size),false);
    }

    // User details
    @GetMapping("/users/{accountId}")
    public ApiResponse<UserProfileResponse> getUserDetails(@PathVariable Long accountId) {
        return CreateApiResponse.createResponse(userManagementService.getUserDetails(accountId),false);
    }

    // Delete users
    @DeleteMapping("/delete-users")
    public ApiResponse<String> softDeleteUsers(@RequestBody List<Long> accountIds) {
        try {
            userManagementService.softDeleteAccounts(accountIds);
            return CreateApiResponse.createResponse("Accounts successfully soft-deleted.",false);
        } catch (Exception e) {
            throw new ApiException(ErrorCode.BAD_GATEWAY.getStatusCode().value(),e.getMessage());
        }
    }

    // Approve users
    @PostMapping("/users/approve")
    public ApiResponse<String> approveNewUsers(@RequestBody List<Long> accountIds) {
        return userManagementService.approveNewUsers(accountIds);
    }

    @DeleteMapping("/delete-profile-picture")
    public ApiResponse<String> adminDeleteUserProfilePicture(@RequestParam Long accountId) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            try {
                return adminUserManagementService.adminDeleteUserProfilePicture(accountId);
            } catch (Exception e) {
                throw new ApiException(ErrorCode.INTERNAL_SERVER_ERROR.getStatusCode().value(),e.getMessage());
            }
        } else {
            throw new ApiException(ErrorCode.UNAUTHORIZED.getStatusCode().value(),"You are not authorized to perform this action.");
        }
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
        Account account = (Account) authentication.getPrincipal();
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
    public ResponseEntity<?> trainDocument(@RequestBody TrainDocumentRequest request){
        try{
            return documentService.trainDocument(request);
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
}
