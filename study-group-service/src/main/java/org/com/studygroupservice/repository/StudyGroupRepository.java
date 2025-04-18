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

    @Query("SELECT g FROM StudyGroup g WHERE LOWER(g.name) LIKE LOWER(CONCAT('%', :name, '%')) AND g.isPrivate = false")
    List<StudyGroup> searchPublicGroupsByName(String name);

    @Query("SELECT COUNT(m) FROM StudyGroup g JOIN g.members m WHERE g.id = :groupId")
    int getMemberCount(Long groupId);

}
