package com.capstone1.sasscapstone1.repository.History;

import com.capstone1.sasscapstone1.dto.FolderDownloadStatsDto.FolderDownloadStatsDto;
import com.capstone1.sasscapstone1.entity.History;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HistoryRepository extends JpaRepository<History, Long> {
    Optional<History> findByDocument_DocId(Long docId);
    Page<History> findAllByOrderByDownloadCountDesc(Pageable pageable);

    @Query("SELECT new com.capstone1.sasscapstone1.dto.FolderDownloadStatsDto.FolderDownloadStatsDto(" +
            "f.folderId, f.folderName, COUNT(h.historyId), CONCAT(a.firstName, ' ', a.lastName)) " +
            "FROM History h " +
            "JOIN h.document d " +
            "JOIN d.folder f " +
            "JOIN f.account a " +
            "GROUP BY f.folderId, f.folderName, a.firstName, a.lastName " +
            "ORDER BY COUNT(h.historyId) DESC")
    Page<FolderDownloadStatsDto> getTopFoldersByDownloadCount(Pageable pageable);

}
