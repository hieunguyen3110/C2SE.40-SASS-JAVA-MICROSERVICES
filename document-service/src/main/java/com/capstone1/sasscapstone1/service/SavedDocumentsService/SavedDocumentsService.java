package com.capstone1.sasscapstone1.service.SavedDocumentsService;

import com.capstone1.sasscapstone1.dto.SavedDocumentsDto.SavedDocumentsDto;
import org.springframework.data.domain.Page;

public interface SavedDocumentsService {
    void saveDocument(Long docId, Long accountId);
    void deleteSavedDocument(Long savedId, Long accountId);
    Page<SavedDocumentsDto> listSavedDocuments(Long accountId, int page, int size);
}
