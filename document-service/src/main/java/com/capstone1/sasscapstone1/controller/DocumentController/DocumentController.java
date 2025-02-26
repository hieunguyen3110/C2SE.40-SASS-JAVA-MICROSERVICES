package com.capstone1.sasscapstone1.controller.DocumentController;

import com.capstone1.sasscapstone1.dto.AdminDocumentDto.AdminDocumentDto;
import com.capstone1.sasscapstone1.dto.DocumentDetailDto.DocumentDetailDto;
import com.capstone1.sasscapstone1.dto.DocumentDto.DocumentDto;
import com.capstone1.sasscapstone1.dto.PopularDocumentDto.PopularDocumentDto;
import com.capstone1.sasscapstone1.dto.response.ApiResponse;
import com.capstone1.sasscapstone1.entity.Account;
import com.capstone1.sasscapstone1.enums.ErrorCode;
import com.capstone1.sasscapstone1.exception.ApiException;
import com.capstone1.sasscapstone1.service.DocumentService.DocumentService;
import com.capstone1.sasscapstone1.service.UserDetailService.UserDetailServiceImpl;
import com.capstone1.sasscapstone1.util.CreateApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/document")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;
    private final UserDetailServiceImpl userDetailService;

    // Upload tài liệu với môn học và khoa
    @PostMapping("/upload")
    public ApiResponse<String> uploadDocument(@RequestPart("file") MultipartFile file,
                                              @RequestPart("title") String title,
                                              @RequestPart("description") String description,
                                              @Nullable @RequestPart("content") String content,
                                              @RequestPart("type") String type,
                                              @RequestPart("subjectCode") String subjectCode,
                                              @Nullable @RequestPart("facultyName") String facultyName,
                                              @Nullable  @RequestPart("folderId") String folderId) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            Account account = (Account) authentication.getPrincipal();
            try {
                return documentService.uploadDocument(file, title, description, content, type, subjectCode, facultyName, folderId, account);
            } catch (IOException e) {
                throw new ApiException(ErrorCode.INTERNAL_SERVER_ERROR.getStatusCode().value(),"Error uploading file: " + e.getMessage());
            } catch (Exception e) {
                throw new ApiException(ErrorCode.INTERNAL_SERVER_ERROR.getStatusCode().value(),e.getMessage());
            }
        } else {
            throw new ApiException(ErrorCode.FORBIDDEN.getStatusCode().value(),"You are not authorized to perform this action.");
        }
    }

    // Lấy tất cả tài liệu
    @GetMapping("/all")
    @Transactional
    public ApiResponse<Page<DocumentDto>> getAllDocuments(@RequestParam(defaultValue = "0") int page,
                                                             @RequestParam(defaultValue = "3") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<DocumentDto> documentPage = documentService.getAllDocuments(pageable);
        return CreateApiResponse.createResponse(documentPage,false);
    }

    @GetMapping("/folder")
    @Transactional
    public ApiResponse<List<DocumentDto>> getAllDocumentsByFolderId(@RequestParam("folderId") Long folderId,
                                                       @RequestParam(value = "pageNum", defaultValue = "0") int pageNum,
                                                       @RequestParam(value = "pageSize", defaultValue = "10") int pageSize
                                                       ) {
        Authentication authentication= SecurityContextHolder.getContext().getAuthentication();
        if(!(authentication instanceof AnonymousAuthenticationToken)){
            return documentService.getDocumentByFolderId(folderId,pageNum,pageSize);
        }else{
            throw new ApiException(ErrorCode.FORBIDDEN.getStatusCode().value(),"You are not authorized to perform this action.");
        }
    }

    // Lấy tài liệu theo ID
    @GetMapping("/{docId}")
    public ApiResponse<DocumentDetailDto> getDocumentById(@PathVariable Long docId) {
        try {
            DocumentDetailDto documentDetail = documentService.getDocumentById(docId);
            return CreateApiResponse.createResponse(documentDetail,false);
        } catch (RuntimeException e) {
            throw new ApiException(ErrorCode.BAD_GATEWAY.getStatusCode().value(),e.getMessage());
        } catch (Exception e) {
            throw new ApiException(ErrorCode.INTERNAL_SERVER_ERROR.getStatusCode().value(),e.getMessage());
        }
    }

    @GetMapping("/account")
    @Transactional
    public ApiResponse<List<DocumentDto>> getAllDocumentsByAccount(
                                                      @RequestParam(value = "pageNum", defaultValue = "0") int pageNum,
                                                      @RequestParam(value = "pageSize", defaultValue = "10") int pageSize
    ) {
        try{
            Authentication authentication= SecurityContextHolder.getContext().getAuthentication();
            if(!(authentication instanceof AnonymousAuthenticationToken)){
                Account account= (Account) authentication.getPrincipal();
                return documentService.findAllByAccount(account,pageNum,pageSize);
            }else{
                throw new ApiException(ErrorCode.FORBIDDEN.getStatusCode().value(),"You are not authorized to perform this action.");
            }
        }catch (Exception e){
            throw new ApiException(ErrorCode.INTERNAL_SERVER_ERROR.getStatusCode().value(),e.getMessage());
        }
    }

    @GetMapping("/account/email")
    @Transactional
    public ApiResponse<List<DocumentDto>> getAllDocumentsByEmail(@RequestParam("email") String email,
                                                       @RequestParam(value = "pageNum", defaultValue = "0") int pageNum,
                                                       @RequestParam(value = "pageSize", defaultValue = "10") int pageSize
    ) {
        try{
            Authentication authentication= SecurityContextHolder.getContext().getAuthentication();
            if(!(authentication instanceof AnonymousAuthenticationToken)){
                Account account= (Account) userDetailService.loadUserByUsername(email);
                return documentService.findAllByAccount(account,pageNum,pageSize);
            }else{
                throw new ApiException(ErrorCode.FORBIDDEN.getStatusCode().value(),"You are not authorized to perform this action.");
            }
        }catch (Exception e){
            throw new ApiException(ErrorCode.INTERNAL_SERVER_ERROR.getStatusCode().value(),e.getMessage());
        }
    }

    @PutMapping("/{docId}")
    public ApiResponse<String> updateDocument(@PathVariable Long docId,
                                            @RequestBody AdminDocumentDto documentDto) {
        documentService.updateDocument(docId, documentDto);
        return CreateApiResponse.createResponse("Document updated successfully.",false);
    }

    @GetMapping("/popular")
    public ApiResponse<Page<PopularDocumentDto>> getPopularDocuments(@RequestParam(value = "page", defaultValue = "0") int page,
                                                                        @RequestParam(value = "size", defaultValue = "3") int size) {
        Page<PopularDocumentDto> popularDocuments = documentService.getPopularDocuments(page, size);
        return CreateApiResponse.createResponse(popularDocuments,false);
    }

}
