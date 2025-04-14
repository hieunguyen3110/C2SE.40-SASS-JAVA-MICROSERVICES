package com.capstone1.sasscapstone1.repository.History;

import com.capstone1.sasscapstone1.dto.FolderDownloadStatsDto.FolderDownloadStatsDto;
import com.capstone1.sasscapstone1.entity.History;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface HistoryRepository extends JpaRepository<History, Long> {
    Optional<History> findByDocument_DocId(Long docId);
    Page<History> findAllByOrderByDownloadCountDesc(Pageable pageable);
    @Query("select h from History h where h.document.docId=:docId and h.accountId=:accountId and function('DATE',h.createdAt)=:targetDate")
    Optional<History> findByDocIdAndAccountIdAndCreatedAt(@Param("docId") Long docId,
                                              @Param("accountId") Long accountId,
                                              @Param("targetDate") LocalDate targetDate);

    @Query("SELECT new com.capstone1.sasscapstone1.dto.FolderDownloadStatsDto.FolderDownloadStatsDto(" +
            "f.folderId, f.folderName, COUNT(h.historyId), 'N/A') " +
            "FROM History h " +
            "JOIN h.document d " +
            "JOIN d.folder f " +
            "GROUP BY f.folderId, f.folderName " +
            "ORDER BY COUNT(h.historyId) DESC")
    Page<FolderDownloadStatsDto> getTopFoldersByDownloadCount(Pageable pageable);
    List<History> findAllByDocument_DocIdAndCreatedAtBetween(Long docId, LocalDateTime startDate, LocalDateTime endDate);
    Optional<History> findByAccountId(Long accountId);
    List<History> findAllByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

}
