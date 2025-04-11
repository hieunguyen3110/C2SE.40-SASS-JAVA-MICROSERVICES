package org.com.elearningservice.service;

import org.com.elearningservice.dto.request.AssessmentRequest;
import org.com.elearningservice.dto.request.AssignmentRequest;
import org.com.elearningservice.dto.request.CreateTestRequest;
import org.com.elearningservice.dto.response.QuestionDto;
import org.com.elearningservice.dto.response.QuizSession;

import java.util.List;

public interface ELearningService {
    String analyzeLearningStyle(AssessmentRequest request);
    List<QuestionDto> generateQuizQuestions(CreateTestRequest request);
    List<QuestionDto> createAssignmentFromDocument(AssignmentRequest request);
    QuizSession getQuizSession(Long userId, Long quizId);
    void saveQuizSession(QuizSession quizSession);
    void updateQuizAnswer(Long userId, Long quizId, Integer questionId, String answer);
}
