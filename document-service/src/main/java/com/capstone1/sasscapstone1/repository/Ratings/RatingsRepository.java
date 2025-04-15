package com.capstone1.sasscapstone1.repository.Ratings;

import com.capstone1.sasscapstone1.entity.Ratings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RatingsRepository extends JpaRepository<Ratings,Long> {
    @Query("select r from Ratings r where r.createdAt between :startDate and :endDate")
    List<Ratings> findAllByCreatedAtAndIsChecked(@Param("startDate") LocalDateTime startDate,
                                                 @Param("endDate") LocalDateTime endDate);
    Optional<Ratings> findRatingsByAccountIdAndDocuments_DocId(Long accountId,Long docId);

    @Query("select r from Ratings r where r.createdAt between :startDate and :endDate")
    List<Ratings> findAllByCreatedAt(@Param("startDate") LocalDateTime startDate,
                                                       @Param("endDate") LocalDateTime endDate);
    @Query("select r from Ratings r where (r.createdAt between :startDate and :endDate) and r.documents.docId=:docId")
    List<Ratings> findAllByCreatedAtAndDocuments_DocId(@Param("startDate") LocalDateTime startDate,
                                                       @Param("endDate") LocalDateTime endDate,
                                                       @Param("docId") Long docId);

    Optional<Ratings> findByAccountIdAndDocuments_DocIdAndCreatedAtBetween(Long accountId,Long docId, LocalDateTime startDate, LocalDateTime endDate);
}
