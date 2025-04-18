package org.com.studygroupservice.repository;

import org.com.studygroupservice.entity.GroupMember;
import org.com.studygroupservice.entity.StudyGroup;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface GroupMemberRepository extends JpaRepository<GroupMember, Long> {

    Optional<GroupMember> findByStudyGroupIdAndAccountId(Long studyGroupId, Long accountId);

    void deleteByStudyGroupId(Long studyGroupId);

    Page<GroupMember> findByStudyGroupId(Long studyGroupId, Pageable pageable);

    List<GroupMember> findByAccountId(Long accountId);

    List<GroupMember> findStudyGroupsByAccountId(Long accountId);
}
