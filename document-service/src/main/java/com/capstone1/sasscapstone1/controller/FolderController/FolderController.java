package com.capstone1.sasscapstone1.controller.FolderController;

import com.capstone1.sasscapstone1.dto.FolderDownloadStatsDto.FolderDownloadStatsDto;
import com.capstone1.sasscapstone1.dto.FolderDto.FolderDto;
import com.capstone1.sasscapstone1.dto.FolderDto.FolderResponse;
import com.capstone1.sasscapstone1.dto.FolderRequestDto.FolderRequestDto;
import com.capstone1.sasscapstone1.dto.response.ApiResponse;
import com.capstone1.sasscapstone1.entity.Account;
import com.capstone1.sasscapstone1.enums.ErrorCode;
import com.capstone1.sasscapstone1.exception.ApiException;
import com.capstone1.sasscapstone1.service.FolderService.FolderService;
import com.capstone1.sasscapstone1.service.UserDetailService.UserDetailServiceImpl;
import com.capstone1.sasscapstone1.util.CreateApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

@RestController
@RequestMapping("/folder")
@RequiredArgsConstructor
public class FolderController {
    private final FolderService folderService;
    private final UserDetailServiceImpl userDetailService;

    // Tạo folder mới
    @PostMapping("/create")
    public ApiResponse<FolderDto> createFolder(@RequestBody FolderRequestDto request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            Account account = (Account) authentication.getPrincipal();
            FolderDto folderDto = folderService.createFolder(request.getFolderName(), request.getDescription(), account);
            return CreateApiResponse.createResponse(folderDto,true);
        }
        throw new ApiException(ErrorCode.FORBIDDEN.getStatusCode().value(),"Unauthorized");
    }

    @GetMapping("/{folderId}")
    public ApiResponse<FolderResponse> getFolderByIdOfUser(@PathVariable("folderId") Long folderId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            return folderService.getFolderById(folderId);
        }
        throw new ApiException(ErrorCode.FORBIDDEN.getStatusCode().value(),"Unauthorized");
    }

    @GetMapping("/{folderId}/by-email")
    public ApiResponse<FolderResponse> getFolderByIdOfUserOther(@PathVariable("folderId") Long folderId, @RequestParam("email") String email) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            Account account = (Account) userDetailService.loadUserByUsername(email);
            return folderService.getFolderById(account.getAccountId(),folderId);
        }
        throw new ApiException(ErrorCode.FORBIDDEN.getStatusCode().value(),"Unauthorized");
    }

    // Cập nhật folder
    @PutMapping("/update/{folderId}")
    public ApiResponse<FolderDto> updateFolder(@PathVariable Long folderId, @RequestBody FolderRequestDto request) {
        FolderDto folderDto = folderService.updateFolder(folderId, request.getFolderName(), request.getDescription());
        return CreateApiResponse.createResponse(folderDto,false);
    }

    // Xóa folder
    @DeleteMapping("/delete/{folderId}")
    public ApiResponse<String> deleteFolder(@PathVariable Long folderId) {
        folderService.deleteFolder(folderId);
        return CreateApiResponse.createResponse("Folder deleted successfully",false);
    }

    // Lấy tất cả folder của người dùng
    @GetMapping("/all")
    public ApiResponse<List<FolderDto>> getAllFolders() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            Account account = (Account) authentication.getPrincipal();
            List<FolderDto> folders = folderService.getAllFolders(account);
            return CreateApiResponse.createResponse(folders,false);
        }
        throw new ApiException(ErrorCode.FORBIDDEN.getStatusCode().value(),"Unauthorized");
    }

    @GetMapping("/top-folders")
    public ApiResponse<Page<FolderDownloadStatsDto>> getTopFoldersByDownloadCount(@RequestParam(defaultValue = "0") int page,
                                                                                     @RequestParam(defaultValue = "3") int size) {
        Page<FolderDownloadStatsDto> topFolders = folderService.getTopFoldersByDownloadCount(page, size);
        return CreateApiResponse.createResponse(topFolders,false);
    }
}