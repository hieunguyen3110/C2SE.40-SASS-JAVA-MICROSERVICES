package org.com.identityservice.repository;

import org.com.identityservice.entity.Analyze;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AnalyzeRepository extends JpaRepository<Analyze, Long> {
    Optional<Analyze> findByAccount_AccountId(Long accountId);
}
