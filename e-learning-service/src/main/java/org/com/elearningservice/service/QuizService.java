package org.com.elearningservice.service;

import org.com.elearningservice.dto.response.QuizSessionDTO;
import org.com.elearningservice.dto.response.SubjectDTO;
import org.com.elearningservice.entity.Grade;
import org.com.elearningservice.repository.GradeRepository;

import java.util.List;

public interface QuizService {
    List<SubjectDTO> getSubjectsFromRedis();
    QuizSessionDTO startSession(Long subjectId, int numberOfQuestions, int duration, boolean isAssignment);
    QuizSessionDTO restoreSession(boolean isAssignment);
    Grade submitSession(Long subjectId, List<String> userAnswers, boolean isAssignment);
    List<Grade> getHistory();
    QuizSessionDTO updateSessionAnswer(int index, String userAnswers, boolean isAssignment);
}
