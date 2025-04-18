package org.com.studygroupservice.repository;

import org.com.studygroupservice.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    void deleteByGroupId(Long groupId);

    Page<Message> findByGroupIdAndIsPinnedTrue(Long groupId, Pageable pageable);

    Page<Message> findByGroupId(Long groupId, Pageable pageable);
}
