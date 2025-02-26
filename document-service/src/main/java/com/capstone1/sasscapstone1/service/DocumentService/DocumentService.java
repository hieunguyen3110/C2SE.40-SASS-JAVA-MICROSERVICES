package com.capstone1.sasscapstone1.service.DocumentService;

import com.capstone1.sasscapstone1.dto.AdminDocumentDto.AdminDocumentDto;
import com.capstone1.sasscapstone1.dto.DocumentDetailDto.DocumentDetailDto;
import com.capstone1.sasscapstone1.dto.DocumentDto.DocumentDto;
import com.capstone1.sasscapstone1.dto.PopularDocumentDto.PopularDocumentDto;
import com.capstone1.sasscapstone1.dto.response.ApiResponse;
import com.capstone1.sasscapstone1.entity.Account;
import com.capstone1.sasscapstone1.entity.Documents;
import com.capstone1.sasscapstone1.request.TrainDocumentRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.util.List;

public interface DocumentService {
    ApiResponse<String> uploadDocument(MultipartFile file, String title, String description, String content, String type, String subjectCode, String facultyName, String folderId, Account account) throws Exception;

    Page<DocumentDto> getAllDocuments(Pageable pageable);

    DocumentDetailDto getDocumentById(Long docId);

    ApiResponse<List<DocumentDto>> getDocumentByFolderId(Long folderId, int pageNum, int pageSize);

    ResponseEntity<?> trainDocument(TrainDocumentRequest request) throws Exception;

    ApiResponse<List<DocumentDto>> findAllByAccount(Account account, int pageNum, int pageSum) throws Exception;

    void updateDocument(Long docId, AdminDocumentDto documentDto);

    Page<PopularDocumentDto> getPopularDocuments(int page, int size);
}
