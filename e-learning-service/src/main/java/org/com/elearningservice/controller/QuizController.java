package org.com.elearningservice.controller;

import org.com.elearningservice.dto.request.SubmitRequest;
import org.com.elearningservice.dto.request.TestRequest;
import org.com.elearningservice.dto.request.UpdateAnswerRequest;
import org.com.elearningservice.dto.response.ApiResponse;
import org.com.elearningservice.dto.response.GradeDto;
import org.com.elearningservice.dto.response.QuizSessionDTO;
import org.com.elearningservice.dto.response.SubjectDTO;
import org.com.elearningservice.helper.CreateApiResponse;
import org.com.elearningservice.service.QuizService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class QuizController {

    private final QuizService quizService;

    public QuizController(QuizService quizService) {
        this.quizService = quizService;
    }

    @GetMapping("/subjects")
    public ApiResponse<List<SubjectDTO>> getSubjects() {
        List<SubjectDTO> subjects = quizService.getSubjectsFromRedis();
        return CreateApiResponse.createResponse(subjects, false);
    }

    @PostMapping("/quiz/start")
    public ApiResponse<QuizSessionDTO> startQuiz(@RequestBody TestRequest request) {
        QuizSessionDTO session = quizService.startSession(request.getSubjectId(), request.getNumberOfQuestions(), request.getDuration(), false);
        return CreateApiResponse.createResponse(session, true);
    }

    @PostMapping("/assignment/start")
    public ApiResponse<QuizSessionDTO> startAssignment(@RequestBody TestRequest request) {
        QuizSessionDTO session = quizService
                .startSession(request.getSubjectId(), request.getNumberOfQuestions(), request.getDuration(), true);
        return CreateApiResponse.createResponse(session, true);
    }

    @GetMapping("/session/restore")
    public ApiResponse<QuizSessionDTO> restoreSession(@RequestParam boolean isAssignment) {
        QuizSessionDTO session = quizService.restoreSession(isAssignment);
        return CreateApiResponse.createResponse(session, false);
    }

    @PostMapping("/session/update-answer")
    public ApiResponse<QuizSessionDTO> updateSessionAnswer(@RequestBody UpdateAnswerRequest request) {
        QuizSessionDTO session = quizService.updateSessionAnswer(request.getQuestionIndex(), request.getUserAnswer(), request.getIsAssignment());
        return CreateApiResponse.createResponse(session, false);
    }

    @PostMapping("/submit")
    public ApiResponse<GradeDto> submit(@RequestBody SubmitRequest request) {
        GradeDto result = quizService.submitSession(request.getSubjectId(), request.getUserAnswers(), request.getIsAssignment());
        return CreateApiResponse.createResponse(result, true);
    }

    @GetMapping("/history")
    public ApiResponse<List<GradeDto>> getHistory() {
        List<GradeDto> history = quizService.getHistory();
        return CreateApiResponse.createResponse(history, false);
    }
}
