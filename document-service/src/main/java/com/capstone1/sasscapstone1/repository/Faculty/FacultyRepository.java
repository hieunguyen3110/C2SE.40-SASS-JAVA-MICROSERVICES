package com.capstone1.sasscapstone1.repository.Faculty;

import com.capstone1.sasscapstone1.entity.Faculty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FacultyRepository extends JpaRepository<Faculty, Long> {
    Optional<Faculty> findByFacultyName(String facultyName);

    List<Faculty> findAllByFacultyNameContainingIgnoreCase(String facultyName);

    Optional<Faculty> findByFacultyId(Long facultyId);
}
