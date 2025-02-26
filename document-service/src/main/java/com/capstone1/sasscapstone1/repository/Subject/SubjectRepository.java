package com.capstone1.sasscapstone1.repository.Subject;

import com.capstone1.sasscapstone1.entity.Subject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface SubjectRepository extends JpaRepository<Subject,Long> {
    List<Subject> findAllBySubjectNameContainingIgnoreCase(String subjectName);
    Optional<Subject> findBySubjectCode(String subjectCode);
    Optional<Subject> findBySubjectName(String subjectName);
}
