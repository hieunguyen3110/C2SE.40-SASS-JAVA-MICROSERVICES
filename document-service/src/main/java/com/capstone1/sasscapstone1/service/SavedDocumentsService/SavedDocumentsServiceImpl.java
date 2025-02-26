package com.capstone1.sasscapstone1.service.SavedDocumentsService;

import com.capstone1.sasscapstone1.dto.SavedDocumentsDto.SavedDocumentsDto;
import com.capstone1.sasscapstone1.entity.Account;
import com.capstone1.sasscapstone1.entity.Documents;
import com.capstone1.sasscapstone1.entity.SavedDocuments;
import com.capstone1.sasscapstone1.enums.ErrorCode;
import com.capstone1.sasscapstone1.exception.ApiException;
import com.capstone1.sasscapstone1.repository.Account.AccountRepository;
import com.capstone1.sasscapstone1.repository.Documents.DocumentsRepository;
import com.capstone1.sasscapstone1.repository.SavedDocuments.SavedDocumentsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class SavedDocumentsServiceImpl implements SavedDocumentsService {

    private final SavedDocumentsRepository savedDocumentsRepository;
    private final DocumentsRepository documentsRepository;
    private final AccountRepository accountRepository;

    @Autowired
    public SavedDocumentsServiceImpl(SavedDocumentsRepository savedDocumentsRepository,
                                     DocumentsRepository documentsRepository,
                                     AccountRepository accountRepository) {
        this.savedDocumentsRepository = savedDocumentsRepository;
        this.documentsRepository = documentsRepository;
        this.accountRepository = accountRepository;
    }

    @Override
    public void saveDocument(Long docId, Long accountId) {
        try {
            // Kiểm tra tài liệu
            Documents document = documentsRepository.findById(docId)
                    .orElseThrow(() -> new ApiException(ErrorCode.BAD_REQUEST.getStatusCode().value(),"Document not found with ID: " + docId));

            // Kiểm tra tài liệu đã được lưu hay chưa
            Optional<SavedDocuments> existingSavedDocument = savedDocumentsRepository.findByAccount_AccountIdAndDocument_DocId(accountId, docId);
            if (existingSavedDocument.isPresent()) {
                throw new ApiException(ErrorCode.BAD_GATEWAY.getStatusCode().value(),"Document already saved.");
            }

            // Lấy tài khoản từ accountId
            Account account = accountRepository.findById(accountId)
                    .orElseThrow(() -> new ApiException(ErrorCode.BAD_REQUEST.getStatusCode().value(),"Account not found with ID: " + accountId));

            // Lưu tài liệu
            SavedDocuments savedDocument = new SavedDocuments();
            savedDocument.setAccount(account);
            savedDocument.setDocument(document);

            savedDocumentsRepository.save(savedDocument);
        } catch (Exception e) {
            throw new RuntimeException("Error saving document: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public void deleteSavedDocument(Long docId, Long accountId) {
        try {
            // Kiểm tra tài khoản và tài liệu đã lưu
            Optional<SavedDocuments> savedDocument = savedDocumentsRepository.findByAccount_AccountIdAndDocument_DocId(accountId, docId);
            if (savedDocument.isEmpty()) {
                throw new ApiException(ErrorCode.BAD_REQUEST.getStatusCode().value(),"Saved document not found for the given account and document ID.");
            }

            // Xóa tài liệu đã lưu
            savedDocumentsRepository.deleteByAccount_AccountIdAndDocument_DocId(accountId, docId);
        } catch (Exception e) {
            throw new RuntimeException("Error deleting saved document: " + e.getMessage(), e);
        }
    }

    @Override
    public Page<SavedDocumentsDto> listSavedDocuments(Long accountId, int page, int size) {
        try {
            Page<SavedDocuments> savedDocumentsPage = savedDocumentsRepository.findByAccount_AccountId(accountId, PageRequest.of(page, size));

            // Chuyển đổi entity sang DTO
            return savedDocumentsPage.map(savedDocument -> {
                SavedDocumentsDto dto = new SavedDocumentsDto();
                dto.setSavedId(savedDocument.getSavedId());
                dto.setDocId(savedDocument.getDocument().getDocId());
                dto.setTitle(savedDocument.getDocument().getTitle());
                dto.setDescription(savedDocument.getDocument().getDescription());
                dto.setFilePath(savedDocument.getDocument().getFilePath());
                return dto;
            });
        } catch (Exception e) {
            throw new RuntimeException("Error listing saved documents: " + e.getMessage(), e);
        }
    }
}
