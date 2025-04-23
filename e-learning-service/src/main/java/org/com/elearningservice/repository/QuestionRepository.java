package org.com.elearningservice.repository;

import org.com.elearningservice.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {
    List<Question> findBySubjectId(Long subjectId);

    Optional<Question> findByQuestionTextAndSubjectId(String question, Long subjectId);
}
