package com.capstone1.sasscapstone1.repository.DocumentShares;

import com.capstone1.sasscapstone1.entity.DocumentShares;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.List;

public interface DocumentSharesRepository extends JpaRepository<DocumentShares, Long> {
    List<DocumentShares> findByEmail(String email);
    List<DocumentShares> findByDocument_DocId(Long docId);
}
