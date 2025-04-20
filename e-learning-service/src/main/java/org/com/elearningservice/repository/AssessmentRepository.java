package org.com.elearningservice.repository;

import org.com.elearningservice.entity.Assessment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AssessmentRepository extends JpaRepository<Assessment, Long> {
    Optional<Assessment> findByAccountId(Long accountId);
}
