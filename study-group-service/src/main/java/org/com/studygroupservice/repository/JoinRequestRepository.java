package org.com.studygroupservice.repository;

import org.com.studygroupservice.entity.JoinRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface JoinRequestRepository extends JpaRepository<JoinRequest, Long> {

    @Query("SELECT jr FROM JoinRequest jr WHERE jr.studyGroup.id = :groupId AND jr.status = :status")
    List<JoinRequest> findByStudyGroupIdAndStatus(Long groupId, JoinRequest.RequestStatus status);
    Optional<JoinRequest> findByStudyGroupIdAndAccountId(Long groupId, Long accountId);

    void deleteJoinRequestsByStudyGroup_Id(Long studyGroupId);
}
