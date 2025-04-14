package com.capstone1.sasscapstone1.repository.DocumentView;

import com.capstone1.sasscapstone1.entity.DocumentView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface DocumentViewRepository extends JpaRepository<DocumentView,Long> {
    Optional<DocumentView> findByAccountIdAndDocumentId(Long accountId, Long documentId);
    List<DocumentView> findAllByDocumentIdAndStartViewTimeBetween(Long documentId, LocalDateTime startDate, LocalDateTime endDate);
    List<DocumentView> findAllByAccountIdAndDocumentIdAndCreatedAtBetween(Long accountId,Long documentId, LocalDateTime startDate, LocalDateTime endDate);
    List<DocumentView> findAllByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
}
