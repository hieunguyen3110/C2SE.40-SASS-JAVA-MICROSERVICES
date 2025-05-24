package org.com.studygroupservice.repository;

import org.com.studygroupservice.entity.GroupMember;
import org.com.studygroupservice.entity.StudyGroup;
import org.com.studygroupservice.enums.GroupMemberRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface GroupMemberRepository extends JpaRepository<GroupMember, Long> {

    Optional<GroupMember> findByStudyGroupIdAndAccountId(Long studyGroupId, Long accountId);

    void deleteByStudyGroupId(Long studyGroupId);

    Page<GroupMember> findByStudyGroupId(Long studyGroupId, Pageable pageable);
    List<GroupMember> findByStudyGroupId(Long groupId);

    List<GroupMember> findByAccountId(Long accountId);

    List<GroupMember> findStudyGroupsByAccountId(Long accountId);

    Long countByAccountId(Long accountId);

    List<GroupMember> findByStudyGroupIdAndRole(Long groupId, GroupMemberRole groupMemberRole);
}
