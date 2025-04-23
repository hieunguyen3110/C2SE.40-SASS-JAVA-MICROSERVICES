package org.com.studygroupservice.repository;

import org.com.studygroupservice.entity.GroupMember;
import org.com.studygroupservice.entity.StudyGroup;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface StudyGroupRepository extends JpaRepository<StudyGroup, Long> {
    List<StudyGroup> findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(String nameKeyword, String descriptionKeyword);

    List<StudyGroup> findByIsPrivateFalse();

    List<StudyGroup> findByIsPrivateTrue();

    @NotNull Optional<StudyGroup> findById(@NotNull Long groupId);

    Optional<StudyGroup> findByIdAndIsPrivateFalse(Long l);

    List<StudyGroup> searchGroupByNameContainingIgnoreCase(String name);

    @Query("SELECT COUNT(m) FROM StudyGroup g JOIN g.members m WHERE g.id = :groupId")
    int getMemberCount(Long groupId);

}
