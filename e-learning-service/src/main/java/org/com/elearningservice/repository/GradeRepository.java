package org.com.elearningservice.repository;

import org.com.elearningservice.entity.Grade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Repository
public interface GradeRepository extends JpaRepository<Grade, Long> {

    List<Grade> findByAccountId(Long userId);

    @Query("select g from Grade g where " +
            "g.accountId in :accountIds and " +
            "g.createdAt between :startDate and :endDate")
    List<Grade> findByAccountIdsAndCreatedAtBetween(@Param("accountIds") List<Long> accountIds,
                                                    @Param("startDate")LocalDateTime startDate,
                                                    @Param("endDate") LocalDateTime endDate);

     default List<Grade> findByUserIdAndQuizId(Long userId, Long quizId) {
        return findByAccountId(userId).stream()
                .filter(grade -> quizId == null || (grade.getQuiz() != null && grade.getQuiz().getId().equals(quizId)))
                .collect(Collectors.toList());
    }

    default List<Grade> findByUserIdAndAssignmentId(Long userId, Long assignmentId) {
        return findByAccountId(userId).stream()
                .filter(grade -> assignmentId == null || (grade.getAssignment() != null && grade.getAssignment().getId().equals(assignmentId)))
                .collect(Collectors.toList());
    }
}
