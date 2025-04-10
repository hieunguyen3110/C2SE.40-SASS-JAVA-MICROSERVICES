package org.com.elearningservice.service;

import org.com.elearningservice.dto.request.AssessmentRequest;
import org.com.elearningservice.dto.request.AssignmentRequest;
import org.com.elearningservice.dto.request.CreateTestRequest;
import org.com.elearningservice.dto.response.QuestionDto;

import java.util.List;

public interface ELearningService {
    String analyzeLearningStyle(AssessmentRequest request);
    List<QuestionDto> generateQuizQuestions(CreateTestRequest request);
    List<QuestionDto> createAssignmentFromDocument(AssignmentRequest request);

}
