package com.capstone1.sasscapstone1.service.DocumentSharesService;

import com.capstone1.sasscapstone1.dto.response.ApiResponse;
import com.capstone1.sasscapstone1.entity.DocumentShares;
import com.capstone1.sasscapstone1.entity.Documents;
import com.capstone1.sasscapstone1.entity.Folder;
import com.capstone1.sasscapstone1.repository.DocumentShares.DocumentSharesRepository;
import com.capstone1.sasscapstone1.repository.Documents.DocumentsRepository;
import com.capstone1.sasscapstone1.repository.Folder.FolderRepository;
import com.capstone1.sasscapstone1.util.CreateApiResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class DocumentSharesServiceImpl implements DocumentSharesService {

    private final DocumentSharesRepository documentShareRepository;
    private final DocumentsRepository documentsRepository;
    private final FolderRepository foldersRepository;

    public DocumentSharesServiceImpl(DocumentSharesRepository documentShareRepository,
                                     DocumentsRepository documentsRepository,
                                     FolderRepository foldersRepository) {
        this.documentShareRepository = documentShareRepository;
        this.documentsRepository = documentsRepository;
        this.foldersRepository = foldersRepository;
    }

    @Override
    public ApiResponse<DocumentShares> shareDocument(Long documentId, Long folderId, String email, String shareUrl) throws Exception {
        try {
            Documents document = documentsRepository.findById(documentId)
                    .orElseThrow(() -> new RuntimeException("Document not found"));

            Folder folder = folderId != null ? foldersRepository.findById(folderId)
                    .orElseThrow(() -> new RuntimeException("Folder not found")) : null;

            DocumentShares documentShare = new DocumentShares();
            documentShare.setDocument(document);
            documentShare.setFolderId(folder);
            documentShare.setEmail(email);
            documentShare.setShareUrl(shareUrl);
            documentShareRepository.save(documentShare);

            return CreateApiResponse.createResponse(documentShare,false);
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    @Override
    public ApiResponse<List<DocumentShares>> getSharesByEmail(String email) throws Exception {
        try {
            List<DocumentShares> shares = documentShareRepository.findByEmail(email);
            return CreateApiResponse.createResponse(shares,false);
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    @Override
    public ApiResponse<List<DocumentShares>> getSharesByDocument(Long documentId) throws Exception {
        try {
            List<DocumentShares> shares = documentShareRepository.findByDocument_DocId(documentId);
            return CreateApiResponse.createResponse(shares,false);
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }
}
