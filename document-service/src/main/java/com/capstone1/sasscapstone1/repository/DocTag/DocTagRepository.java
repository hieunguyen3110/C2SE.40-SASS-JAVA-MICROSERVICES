package com.capstone1.sasscapstone1.repository.DocTag;

import com.capstone1.sasscapstone1.entity.DocTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DocTagRepository extends JpaRepository<DocTag,Long> {

    // Lấy tất cả các tag liên quan đến một tài liệu
    List<DocTag> findByDocuments_DocId(Long docId);

    // Lấy tất cả các tài liệu liên quan đến một tag
    Optional<DocTag> findByDocuments_DocIdAndTags_TagId(Long docId, Long tagId);
}
