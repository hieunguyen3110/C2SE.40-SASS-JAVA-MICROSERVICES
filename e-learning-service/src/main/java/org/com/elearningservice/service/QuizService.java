package org.com.elearningservice.service;

import org.com.elearningservice.dto.request.StartAssignmentRequest;
import org.com.elearningservice.dto.response.GradeDto;
import org.com.elearningservice.dto.response.QuizSessionDTO;
import org.com.elearningservice.dto.response.SubjectDTO;

import java.util.List;

public interface QuizService {
    List<SubjectDTO> getSubjectsFromRedis();
    QuizSessionDTO startSession(Long subjectId, int numberOfQuestions, int duration, boolean isAssignment);
    QuizSessionDTO startAssignmentWithDoc(StartAssignmentRequest request) throws Exception;
    QuizSessionDTO restoreSession(boolean isAssignment);
    GradeDto submitSession(Long subjectId, List<String> userAnswers, Boolean isAssignment, Long docId);
    List<GradeDto> getHistory();
    QuizSessionDTO updateSessionAnswer(int index, String userAnswers, boolean isAssignment);
}
