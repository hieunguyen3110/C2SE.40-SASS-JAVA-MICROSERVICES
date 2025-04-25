package com.capstone1.sasscapstone1.service.DocumentSharesService;

import com.capstone1.sasscapstone1.dto.AccountDto.AccountDto;
import com.capstone1.sasscapstone1.dto.response.ApiResponse;
import com.capstone1.sasscapstone1.entity.DocumentShares;
import com.capstone1.sasscapstone1.entity.Documents;
import com.capstone1.sasscapstone1.entity.Folder;
import com.capstone1.sasscapstone1.repository.DocumentShares.DocumentSharesRepository;
import com.capstone1.sasscapstone1.repository.Documents.DocumentsRepository;
import com.capstone1.sasscapstone1.repository.Folder.FolderRepository;
import com.capstone1.sasscapstone1.repository.httpClient.StudyGroupClient;
import com.capstone1.sasscapstone1.util.CreateApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
@RequiredArgsConstructor
public class DocumentSharesServiceImpl implements DocumentSharesService {

    private final DocumentSharesRepository documentShareRepository;
    private final DocumentsRepository documentsRepository;
    private final FolderRepository foldersRepository;
//    private final StudyGroupClient studyGroupClient;

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

    @Override
    public ApiResponse<String> shareDocumentToGroup(Long documentId, Long groupId, String shareUrl) throws Exception {
        try {
            Documents document = documentsRepository.findById(documentId)
                    .orElseThrow(() -> new RuntimeException("Document not found"));

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                throw new RuntimeException("Người dùng chưa đăng nhập.");
            }
            AccountDto accountDto = (AccountDto) authentication.getPrincipal();
            Long senderId = accountDto.getAccountId();

            Map<String, Object> requestBody = createRequestBody(documentId, shareUrl, senderId);

//            String response = studyGroupWebClient.post()
//                    .uri("/groups/{groupId}/share-document", groupId)
//                    .bodyValue(requestBody)
//                    .retrieve()
//                    .bodyToMono(String.class)
//                    .block();

            return CreateApiResponse.createResponse("response", false);
        } catch (Exception e) {
            throw new Exception("Lỗi khi chia sẻ tài liệu vào nhóm: " + e.getMessage());
        }
    }

    private Map<String, Object> createRequestBody(Long documentId, String shareUrl, Long senderId) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("documentId", documentId);
        requestBody.put("shareUrl", shareUrl);
        requestBody.put("senderId", senderId);
        return requestBody;
    }
}
