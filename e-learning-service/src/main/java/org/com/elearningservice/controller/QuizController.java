package org.com.elearningservice.controller;

import org.com.elearningservice.dto.response.ApiResponse;
import org.com.elearningservice.dto.response.QuizSessionDTO;
import org.com.elearningservice.dto.response.SubjectDTO;
import org.com.elearningservice.entity.Grade;
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
    public ApiResponse<QuizSessionDTO> startQuiz(@RequestParam Long subjectId, @RequestParam int numberOfQuestions,
                                                 @RequestParam int duration) {
        QuizSessionDTO session = quizService.startSession(subjectId, numberOfQuestions, duration, false);
        return CreateApiResponse.createResponse(session, true);
    }

    @PostMapping("/assignment/start")
    public ApiResponse<QuizSessionDTO> startAssignment(@RequestParam Long subjectId,
                                                       @RequestParam int numberOfQuestions,
                                                       @RequestParam int duration) {
        QuizSessionDTO session = quizService.startSession(subjectId, numberOfQuestions, duration, true);
        return CreateApiResponse.createResponse(session, true);
    }

    @GetMapping("/session/restore")
    public ApiResponse<QuizSessionDTO> restoreSession(@RequestParam boolean isAssignment) {
        QuizSessionDTO session = quizService.restoreSession(isAssignment);
        return CreateApiResponse.createResponse(session, false);
    }

    @PostMapping("/session/update-answer")
    public ApiResponse<QuizSessionDTO> updateSessionAnswer(@RequestBody List<String> userAnswers,
                                                            @RequestParam boolean isAssignment) {
        QuizSessionDTO session = quizService.updateSessionAnswer(userAnswers, isAssignment);
        return CreateApiResponse.createResponse(session, false);
    }

    @PostMapping("/submit")
    public ApiResponse<Grade> submit(@RequestParam Long subjectId, @RequestBody List<String> userAnswers,
                                     @RequestParam boolean isAssignment) {
        Grade result = quizService.submitSession(subjectId, userAnswers, isAssignment);
        return CreateApiResponse.createResponse(result, true);
    }

    @GetMapping("/history")
    public ApiResponse<List<Grade>> getHistory() {
        List<Grade> history = quizService.getHistory();
        return CreateApiResponse.createResponse(history, false);
    }
}
