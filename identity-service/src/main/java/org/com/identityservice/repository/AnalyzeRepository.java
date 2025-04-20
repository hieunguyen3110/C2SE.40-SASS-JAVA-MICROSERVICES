package org.com.identityservice.repository;

import org.com.identityservice.entity.Analyze;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AnalyzeRepository extends JpaRepository<Analyze, Long> {
}
