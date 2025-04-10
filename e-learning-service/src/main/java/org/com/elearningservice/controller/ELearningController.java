package org.com.elearningservice.controller;

import lombok.RequiredArgsConstructor;
import org.com.elearningservice.dto.request.AssessmentRequest;
import org.com.elearningservice.dto.request.AssignmentRequest;
import org.com.elearningservice.dto.request.CreateTestRequest;
import org.com.elearningservice.dto.response.ApiResponse;
import org.com.elearningservice.dto.response.QuestionDto;
import org.com.elearningservice.helper.CreateApiResponse;
import org.com.elearningservice.service.ELearningService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ELearningController {
    private final ELearningService eLearningService;

    @PostMapping("/analyze-learning-style")
    public ApiResponse<String> analyzeLearningStyle(@RequestBody AssessmentRequest request) {
        String analysisResult = eLearningService.analyzeLearningStyle(request);
        return CreateApiResponse.createResponse(analysisResult, true);
    }

    @PostMapping("/create-new-test")
    public ApiResponse<List<QuestionDto>> createNewTest(@RequestBody CreateTestRequest request) {
        List<QuestionDto> questions = eLearningService.generateQuizQuestions(request);
        return CreateApiResponse.createResponse(questions, true);
    }


    @PostMapping("/create-and-get-assignment-questions")
    public ApiResponse<List<QuestionDto>> createAssignmentFromDocument(@RequestBody AssignmentRequest request) {
        List<QuestionDto> questions = eLearningService.createAssignmentFromDocument(request);
        return CreateApiResponse.createResponse(questions, true);
    }
}
