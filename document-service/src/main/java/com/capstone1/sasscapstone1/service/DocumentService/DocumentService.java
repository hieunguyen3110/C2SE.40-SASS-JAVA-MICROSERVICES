package com.capstone1.sasscapstone1.service.DocumentService;

import com.capstone1.sasscapstone1.dto.AccountDto.AccountDto;
import com.capstone1.sasscapstone1.dto.AdminDocumentDto.AdminDocumentDto;
import com.capstone1.sasscapstone1.dto.DocumentDetailDto.DocumentDetailDto;
import com.capstone1.sasscapstone1.dto.DocumentDto.DocumentDto;
import com.capstone1.sasscapstone1.dto.PopularDocumentDto.PopularDocumentDto;
import com.capstone1.sasscapstone1.dto.response.ApiResponse;
import com.capstone1.sasscapstone1.dto.response.DocumentData;
import com.capstone1.sasscapstone1.request.TrainDocumentRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface DocumentService {
    ApiResponse<String> uploadDocument(MultipartFile file, String title, String description, String content, String type, String subjectCode, String facultyName, String folderId, AccountDto account) throws Exception;

    Page<DocumentDto> getAllDocuments(Pageable pageable);

    DocumentDetailDto getDocumentById(Long docId);

    ApiResponse<List<DocumentDto>> getDocumentByFolderId(Long folderId, int pageNum, int pageSize);

    ApiResponse<String> trainDocument(TrainDocumentRequest request) throws Exception;

    ApiResponse<List<DocumentDto>> findAllByAccount(AccountDto account, int pageNum, int pageSize) throws Exception;
    ApiResponse<List<DocumentDto>> findAllByAccount(String email, int pageNum, int pageSize) throws Exception;

    void updateDocument(Long docId, AdminDocumentDto documentDto);

    Page<PopularDocumentDto> getPopularDocuments(int page, int size);
    List<DocumentDto> getAllDocumentByDay() throws Exception;
    String updateFileStatus(List<Long> docIds) throws Exception;
}
