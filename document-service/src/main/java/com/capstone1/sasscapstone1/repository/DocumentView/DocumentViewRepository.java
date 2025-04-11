package com.capstone1.sasscapstone1.repository.DocumentView;

import com.capstone1.sasscapstone1.entity.DocumentView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DocumentViewRepository extends JpaRepository<DocumentView,Long> {
    Optional<DocumentView> findByAccountIdAndDocumentId(Long accountId, Long documentId);
}
