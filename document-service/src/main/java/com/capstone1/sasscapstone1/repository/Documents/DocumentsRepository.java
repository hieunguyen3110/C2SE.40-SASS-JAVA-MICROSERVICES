package com.capstone1.sasscapstone1.repository.Documents;

import com.capstone1.sasscapstone1.entity.Account;
import com.capstone1.sasscapstone1.entity.Documents;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DocumentsRepository extends JpaRepository<Documents,Long> {
    List<Documents> findByFolder_FolderId(Long folderId);
    Page<Documents> findByFolder_FolderIdOrderByCreatedAtDesc(Long folderId,Pageable pageable);
    Page<Documents> findAllByAccount_AccountIdAndIsActiveIsTrue(Long accountId, Pageable pageable);
    Page<Documents> findByFolder_FolderIdAndTitleContainingIgnoreCaseAndIsActiveIsTrue(Long folderId, String title, Pageable pageable);
    Page<Documents> findByFolder_FolderIdAndIsActiveIsTrue(Long folderId, Pageable pageable);

    // Tìm kiếm theo tên tài liệu
    List<Documents> findByTitleContainingIgnoreCaseAndIsActiveIsTrue(String title);

    // Tìm kiếm theo môn học
    List<Documents> findBySubject_SubjectNameContainingIgnoreCaseAndIsActiveIsTrue(String subjectName);

    // Tìm kiếm theo tên thư mục
    @Query("SELECT d FROM Documents d JOIN d.folder f WHERE LOWER(f.folderName) LIKE LOWER(CONCAT('%', :folderName, '%')) and d.isActive=true")
    List<Documents> findByFolderName(String folderName);

    // Tìm kiếm theo tên khoa
    @Query("SELECT d FROM Documents d JOIN d.faculty f WHERE LOWER(f.facultyName) LIKE LOWER(CONCAT('%', :facultyName, '%')) and d.isActive=true")
    List<Documents> findByFacultyName(String facultyName);

    long countByFolder_FolderIdAndIsActiveIsTrue(Long folderId);

    Optional<Documents> findByFileNameAndIsActiveIsTrue(String fileName);

    // Lọc tài liệu chưa bị xóa
    @Query("SELECT d FROM Documents d WHERE d.isDeleted = false ORDER BY d.updatedAt DESC")
    Page<Documents> findActiveDocuments(Pageable pageable);

    // Tìm kiếm tài liệu chưa xóa theo từ khóa
    @Query("SELECT d FROM Documents d WHERE d.isDeleted = false AND d.title LIKE %:keyword%")
    Page<Documents> searchActiveDocuments(String keyword, Pageable pageable);

    Page<Documents> findAllByAccountAndIsActiveIsTrue(Account account, Pageable pageable);

    Optional<Documents> findByDocIdAndIsCheckTrue(Long docId) throws Exception;

    Optional<Documents> findByFilePath(String filePath);

    Optional<Documents> findByDocIdAndIsCheckIsTrue(Long docId);
}
