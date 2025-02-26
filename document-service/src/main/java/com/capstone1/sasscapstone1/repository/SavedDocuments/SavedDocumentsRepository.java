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
    Page<SavedDocuments> findByAccount_AccountId(Long accountId, Pageable pageable);
    Optional<SavedDocuments> findByAccount_AccountIdAndDocument_DocId(Long accountId, Long docId);
    void deleteByAccount_AccountIdAndDocument_DocId(Long accountId, Long docId);
}
