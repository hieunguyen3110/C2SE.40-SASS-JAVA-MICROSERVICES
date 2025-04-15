package com.capstone1.sasscapstone1.service.HistoryService;

import com.capstone1.sasscapstone1.entity.Documents;
import com.capstone1.sasscapstone1.entity.History;
import com.capstone1.sasscapstone1.enums.ErrorCode;
import com.capstone1.sasscapstone1.exception.ApiException;
import com.capstone1.sasscapstone1.repository.Documents.DocumentsRepository;
import com.capstone1.sasscapstone1.repository.History.HistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class HistoryServiceImpl implements HistoryService {

    private final HistoryRepository historyRepository;
    private final DocumentsRepository documentsRepository;

    @Autowired
    public HistoryServiceImpl(HistoryRepository historyRepository, DocumentsRepository documentsRepository) {
        this.historyRepository = historyRepository;
        this.documentsRepository = documentsRepository;
    }

    @Override
    public void trackDownload(Long documentId, Long accountId) {
        try {
            Documents document = documentsRepository.findById(documentId)
                    .orElseThrow(() -> new ApiException(ErrorCode.BAD_REQUEST.getStatusCode().value(),"Document not found with ID: " + documentId));
            LocalDate now= LocalDate.now();
            Optional<History> existHistory= historyRepository.findByDocIdAndAccountIdAndCreatedAt(documentId,accountId,now);
            History history;
            if(existHistory.isPresent()){
                history= existHistory.get();
                history.setDownloadCount(history.getDownloadCount()+1);
            }else{
                history= History.builder()
                        .accountId(accountId)
                        .document(document)
                        .downloadCount(1)
                        .build();
            }
            historyRepository.save(history);
        } catch (Exception e) {
            throw new RuntimeException("Error tracking download: " + e.getMessage(), e);
        }
    }



}

