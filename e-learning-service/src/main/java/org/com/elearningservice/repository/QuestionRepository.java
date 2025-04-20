package org.com.elearningservice.repository;

import org.com.elearningservice.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuestionRepository extends JpaRepository<Question, Long> {
    List<Question> findBySubjectId(Long subjectId);
}
