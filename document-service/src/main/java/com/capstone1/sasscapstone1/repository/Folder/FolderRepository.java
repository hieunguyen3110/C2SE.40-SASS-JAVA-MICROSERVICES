package com.capstone1.sasscapstone1.repository.Folder;

import com.capstone1.sasscapstone1.entity.Folder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

@Repository
public interface FolderRepository extends JpaRepository<Folder,Long> {
    List<Folder> findByAccount_AccountId(Long accountId);

    @Query("SELECT f FROM Folder f WHERE f.folderName = :folderName AND f.account.accountId = :accountId")
    Optional<Folder> findByFolderNameAndAccountId(@Param("folderName") String folderName, @Param("accountId") Long accountId);

    @Query("SELECT f FROM Folder f WHERE f.folderId = :folderId AND f.account.accountId = :accountId")
    Optional<Folder> findByFolderIdAndAccountId(@Param("folderId") Long folderId, @Param("accountId") Long accountId);

    Optional<Folder> findByFolderName(String folderName);

    Optional<Folder> findById(Long folderId);
}
