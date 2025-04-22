package org.com.elearningservice.repository;

import org.com.elearningservice.entity.Grade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface GradeRepository extends JpaRepository<Grade, Long> {
    List<Grade> findByAccountId(Long accountId);

    @Query("select g from Grade g where " +
            "g.accountId in :accountIds and " +
            "g.createdAt between :startDate and :endDate")
    List<Grade> findByAccountIdsAndCreatedAtBetween(@Param("accountIds") List<Long> accountIds,
                                                    @Param("startDate")LocalDateTime startDate,
                                                    @Param("endDate") LocalDateTime endDate);

}
