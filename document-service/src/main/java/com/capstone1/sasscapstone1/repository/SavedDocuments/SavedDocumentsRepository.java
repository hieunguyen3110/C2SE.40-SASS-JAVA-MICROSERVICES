package com.capstone1.sasscapstone1.repository.SavedDocuments;

import com.capstone1.sasscapstone1.entity.SavedDocuments;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SavedDocumentsRepository extends JpaRepository<SavedDocuments, Long> {
    Page<SavedDocuments> findByAccountId(Long accountId, Pageable pageable);
    Optional<SavedDocuments> findByAccountIdAndDocument_DocId(Long accountId, Long docId);
    void deleteByAccountIdAndDocument_DocId(Long accountId, Long docId);
}
