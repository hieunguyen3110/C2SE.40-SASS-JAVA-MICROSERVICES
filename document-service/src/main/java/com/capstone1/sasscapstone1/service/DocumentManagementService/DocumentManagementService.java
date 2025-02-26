package com.capstone1.sasscapstone1.service.DocumentManagementService;

import com.capstone1.sasscapstone1.dto.AdminDocumentDto.AdminDocumentDto;
import com.capstone1.sasscapstone1.dto.DocumentDto.DocumentDto;
import com.capstone1.sasscapstone1.dto.DocumentListDto.DocumentListDto;
import com.capstone1.sasscapstone1.entity.Account;
import org.springframework.data.domain.Page;

import java.util.List;

public interface DocumentManagementService {
    Page<DocumentListDto> listDocuments(int page, int size);

    Page<DocumentListDto> searchDocuments(String keyword, int page, int size);

    void softDeleteDocuments(List<Long> docIds);

    void approveDocuments(List<Long> docIds, String adminApprove);

    AdminDocumentDto getDocumentDetails(Long docId);

    void updateDocument(Long docId, AdminDocumentDto documentDto);
}
