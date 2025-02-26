package com.capstone1.sasscapstone1.service.DownloadDocumentService;

import com.capstone1.sasscapstone1.dto.response.ApiResponse;
import com.capstone1.sasscapstone1.entity.Documents;
import com.capstone1.sasscapstone1.enums.ErrorCode;
import com.capstone1.sasscapstone1.exception.ApiException;
import com.capstone1.sasscapstone1.repository.Documents.DocumentsRepository;
import com.capstone1.sasscapstone1.service.HistoryService.HistoryService;
import com.capstone1.sasscapstone1.util.CreateApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DownloadDocumentServiceImpl implements DownloadDocumentService {

    private final DocumentsRepository documentsRepository;
    private final HistoryService historyService;

    @Autowired
    public DownloadDocumentServiceImpl(DocumentsRepository documentsRepository, HistoryService historyService) {
        this.documentsRepository = documentsRepository;
        this.historyService = historyService;
    }

    @Override
    public ApiResponse<String> downloadDocument(Long documentId, String username) throws Exception {
        try {
            // Kiểm tra tài liệu có tồn tại không
            Documents document = documentsRepository.findById(documentId)
                    .orElseThrow(() -> new ApiException(ErrorCode.BAD_REQUEST.getStatusCode().value(),"Document not found"));

            historyService.trackDownload(documentId, username);

            // Trả về filePath của tài liệu
            String filePath = document.getFilePath();

            if (filePath != null && !filePath.isEmpty()) {
                return CreateApiResponse.createResponse(filePath,false);
            } else {
                throw new ApiException(ErrorCode.BAD_REQUEST.getStatusCode().value(),"File path is invalid or not found.");
            }
        } catch (ApiException e) {
            throw new ApiException(e.getCode(),e.getMessage());
        } catch (Exception e) {
            throw new Exception("An unexpected error occurred: " + e.getMessage());
        }
    }
}
