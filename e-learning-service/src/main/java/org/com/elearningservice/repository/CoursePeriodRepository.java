package org.com.elearningservice.repository;

import org.com.elearningservice.entity.CoursePeriod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CoursePeriodRepository extends JpaRepository<CoursePeriod, Long> {
    @Query("select cp from CoursePeriod cp where cp.accountId = :accountId and cp.startDate <= :now and cp.endDate >= :now")
    Optional<CoursePeriod> findByAccountIdAndStartDateAndEndDate(@Param("accountId") Long accountId,
                                                                 @Param("now") LocalDateTime now);

    @Query("select cp from CoursePeriod cp where cp.accountId in :accountIds and cp.startDate <= :now and cp.endDate >= :now")
    List<CoursePeriod> findByAccountIdsAndStartDateAndEndDate(@Param("accountIds") List<Long> accountIds, @Param("now") LocalDateTime now);

}
