package org.com.studygroupservice.repository;

import org.com.studygroupservice.entity.JoinRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface JoinRequestRepository extends JpaRepository<JoinRequest, Long> {
    List<JoinRequest> findByStudyGroupIdAndStatus(Long groupId, JoinRequest.RequestStatus status);
    Optional<JoinRequest> findByStudyGroupIdAndUserId(Long groupId, Long userId);
}
