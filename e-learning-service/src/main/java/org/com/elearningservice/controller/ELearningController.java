package org.com.elearningservice.controller;

import lombok.RequiredArgsConstructor;
import org.com.elearningservice.dto.request.AssessmentRequest;
import org.com.elearningservice.dto.request.AssignmentRequest;
import org.com.elearningservice.dto.request.CreateTestRequest;
import org.com.elearningservice.dto.response.AnalyzeData;
import org.com.elearningservice.dto.response.ApiResponse;
import org.com.elearningservice.dto.response.QuestionDto;
import org.com.elearningservice.dto.response.QuizSession;
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

    @GetMapping("/quiz-session")
    public ApiResponse<QuizSession> getQuizSession(
            @RequestParam Long userId,
            @RequestParam Long quizId) {
        QuizSession quizSession = eLearningService.getQuizSession(userId, quizId);
        return CreateApiResponse.createResponse(quizSession, true);
    }

    @PostMapping("/update-quiz-answer")
    public ApiResponse<String> updateQuizAnswer(@RequestParam Long userId, @RequestParam Long quizId,
                                                @RequestParam Integer questionId, @RequestParam String answer) {
        eLearningService.updateQuizAnswer(userId, quizId, questionId, answer);
        return CreateApiResponse.createResponse("Answer updated successfully", true);
    }

    @GetMapping("/analyze-data")
    public ApiResponse<AnalyzeData> getAnalyzeDataByAccountId(@RequestParam("accountId") Long accountId) throws Exception {
        return CreateApiResponse.createResponse(eLearningService.getAnalyzeDataByAccountId(accountId),false);
    }
}
