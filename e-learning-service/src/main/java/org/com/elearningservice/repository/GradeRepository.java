package org.com.elearningservice.repository;

import org.com.elearningservice.entity.Grade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GradeRepository extends JpaRepository<Grade, Long> {

    List<Grade> findByUserIdAndQuizId(Long userId, Object o);

    List<Grade> findByUserIdAndAssignmentId(Long userId, Object o);
}
