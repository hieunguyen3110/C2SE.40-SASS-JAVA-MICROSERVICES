package org.com.elearningservice.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.com.elearningservice.dto.response.AnalyzeData;
import org.com.elearningservice.dto.response.QuizSession;
import org.springframework.beans.factory.annotation.Value;
import org.com.elearningservice.constant.AppConstant;
import org.com.elearningservice.dto.request.AssessmentRequest;
import org.com.elearningservice.dto.request.AssignmentRequest;
import org.com.elearningservice.dto.request.CreateTestRequest;
import org.com.elearningservice.dto.request.GenerateQuestionsRequest;
import org.com.elearningservice.dto.response.AccountDto;
import org.com.elearningservice.dto.response.QuestionDto;
import org.com.elearningservice.entity.*;
import org.com.elearningservice.enums.ErrorCode;
import org.com.elearningservice.exception.ApiException;
import org.com.elearningservice.repository.*;
import org.com.elearningservice.service.ELearningService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ELearningServiceImpl implements ELearningService {
    private final WebClient.Builder webClientBuilder;
    private final AssessmentRepository assessmentRepository;
    private final QuestionRepository questionRepository;
    private final QuizRepository quizRepository;
    private final AssignmentRepository assignmentRepository;
    private final GradeRepository gradeRepository;
    private final ObjectMapper objectMapper;
    private final RedisServiceImpl redisService;

    @Value("${app.ai-service.url}")
    private String aiServiceUrl;

    @Override
    public String analyzeLearningStyle(AssessmentRequest request){
        try {
            
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if(auth == null || auth.getPrincipal() == null) {
                throw new ApiException(ErrorCode.FORBIDDEN.getStatusCode().value(), "Unauthorized access");
            }

            AccountDto account = (AccountDto) auth.getPrincipal();
            
            Assessment assessment = new Assessment();
            assessment.setAccountId(account.getAccountId());
            assessment.setOnlineCoursesCompleted(request.getOnlineCoursesCompleted());
            assessment.setParticipationInDiscussions(request.getParticipationInDiscussions());
            assessment.setAssignmentCompletionRate(request.getAssignmentCompletionRate());
            assessmentRepository.save(assessment);

            String analysisResult = webClientBuilder.build()
                    .post()
                    .uri(aiServiceUrl + "/api/analyze-learning-style")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            if (analysisResult == null) {
                throw new ApiException(ErrorCode.INTERNAL_SERVER_ERROR.getStatusCode().value(),
                        "Failed to analyze learning style: No response from AI service");
            }

            return analysisResult;
        } catch (Exception e) {
            throw new ApiException(ErrorCode.INTERNAL_SERVER_ERROR.getStatusCode().value(),
                    "Error analyzing learning style: " + e.getMessage());
        }
    }

    @Override
    public List<QuestionDto> generateQuizQuestions(CreateTestRequest request) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if(auth == null || auth.getPrincipal() == null) {
                throw new ApiException(ErrorCode.UNAUTHORIZED.getStatusCode().value(), "Unauthorized access");
            }
            AccountDto account = (AccountDto) auth.getPrincipal();
            Long accountId = account.getAccountId();

            if (request.getNumberOfQuestions() <= 0) {
                throw new ApiException(ErrorCode.BAD_REQUEST.getStatusCode().value(),
                        "Number of questions must be greater than 0");
            }
            if (request.getTestDuration() <= 0) {
                throw new ApiException(ErrorCode.BAD_REQUEST.getStatusCode().value(),
                        "Test duration must be greater than 0");
            }

            Assessment assessment = assessmentRepository.findById(request.getAssessmentId())
                    .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND.getStatusCode().value(),
                            "Assessment not found with ID: " + request.getAssessmentId()));

            Quiz quiz = new Quiz();
            quiz.setAssessment(assessment);
            quiz.setTitle(AppConstant.QUIZ_TITLE_DEFAULT + " - " + request.getSubject());
            quiz = quizRepository.save(quiz);

            AssessmentRequest assessmentRequest = new AssessmentRequest();
            assessmentRequest.setOnlineCoursesCompleted(assessment.getOnlineCoursesCompleted());
            assessmentRequest.setParticipationInDiscussions(assessment.getParticipationInDiscussions());
            assessmentRequest.setAssignmentCompletionRate(assessment.getAssignmentCompletionRate());

            GenerateQuestionsRequest aiRequest = new GenerateQuestionsRequest();
            aiRequest.setAssessment(assessmentRequest);
            aiRequest.setSubjectName(request.getSubject());
            aiRequest.setNumberOfQuestions(request.getNumberOfQuestions());
            aiRequest.setTestDuration(request.getTestDuration());

            List<QuestionDto> questionDtos = webClientBuilder.build()
                    .post()
                    .uri(aiServiceUrl + "/api/assess-and-generate")
                    .bodyValue(aiRequest)
                    .retrieve()
                    .bodyToFlux(QuestionDto.class)
                    .collectList()
                    .block();

            if (questionDtos == null || questionDtos.isEmpty()) {
                throw new ApiException(ErrorCode.INTERNAL_SERVER_ERROR.getStatusCode().value(),
                        "Failed to generate questions: No response from AI service");
            }


            Quiz finalQuiz = quiz;
            List<Question> questions = questionDtos.stream().map(dto -> {
                try {
                    Question q = new Question();
                    q.setQuiz(finalQuiz);
                    q.setContent(dto.getContent());
                    q.setOptions(objectMapper.writeValueAsString(dto.getOptions()));
                    q.setCorrectAnswer(dto.getCorrectAnswer());
                    return q;
                } catch (Exception e) {
                    throw new ApiException(ErrorCode.INTERNAL_SERVER_ERROR.getStatusCode().value(),
                            "Error converting question options to JSON: " + e.getMessage());
                }
            }).collect(Collectors.toList());
            questionRepository.saveAll(questions);

            // Bước 8: Cập nhật exam_score
            updateExamScore(accountId, assessment);

            return questionDtos;
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException(ErrorCode.INTERNAL_SERVER_ERROR.getStatusCode().value(),
                    "Error generating quiz questions: " + e.getMessage());
        }
    }

    @Override
    public List<QuestionDto> createAssignmentFromDocument(AssignmentRequest request) {
        try {

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if(authentication == null || authentication.getPrincipal() == null) {
                throw new ApiException(ErrorCode.UNAUTHORIZED.getStatusCode().value(), "Unauthorized access");
            }
            AccountDto account = (AccountDto) authentication.getPrincipal();
            Long accountId = account.getAccountId();
            if (request.getDocumentId() == null || request.getDocumentId() <= 0) {
                throw new ApiException(ErrorCode.BAD_REQUEST.getStatusCode().value(),
                        "Document ID must be provided and greater than 0");
            }

            Assignment assignment = new Assignment();
            assignment.setDocumentId(request.getDocumentId());
            assignment.setTitle(AppConstant.ASSIGNMENT_TITLE_DEFAULT + request.getDocumentId());
            assignment = assignmentRepository.save(assignment);

            List<QuestionDto> questionDtos = webClientBuilder.build()
                    .post()
                    .uri(aiServiceUrl + "/api/generate-from-document")
                    .bodyValue(new DocumentRequest(request.getDocumentId()))
                    .retrieve()
                    .bodyToFlux(QuestionDto.class)
                    .collectList()
                    .block();

            if (questionDtos == null || questionDtos.isEmpty()) {
                throw new ApiException(ErrorCode.INTERNAL_SERVER_ERROR.getStatusCode().value(),
                        "Failed to generate questions from document: No response from AI service");
            }

            Assignment finalAssignment = assignment;
            List<Question> questions = questionDtos.stream().map(dto -> {
                try {
                    Question q = new Question();
                    q.setAssignment(finalAssignment);
                    q.setContent(dto.getContent());
                    q.setOptions(objectMapper.writeValueAsString(dto.getOptions()));
                    q.setCorrectAnswer(dto.getCorrectAnswer());
                    return q;
                } catch (Exception e) {
                    throw new ApiException(ErrorCode.INTERNAL_SERVER_ERROR.getStatusCode().value(),
                            "Error converting question options to JSON: " + e.getMessage());
                }
            }).collect(Collectors.toList());
            questionRepository.saveAll(questions);

            return questionDtos;
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException(ErrorCode.INTERNAL_SERVER_ERROR.getStatusCode().value(),
                    "Error generating assignment questions: " + e.getMessage());
        }
    }

    @Override
    public QuizSession getQuizSession(Long userId, Long quizId) {
        try {
            String redisKey = "quiz-session:" + userId + ":" + quizId;
            Object session = redisService.getData(redisKey);
            if (session == null) {
                throw new ApiException(ErrorCode.NOT_FOUND.getStatusCode().value(),
                        "Quiz session not found for user " + userId + " and quiz " + quizId);
            }
            return (QuizSession) session;
        } catch (Exception e) {
            throw new ApiException(ErrorCode.INTERNAL_SERVER_ERROR.getStatusCode().value(),
                    "Error retrieving quiz session: " + e.getMessage());
        }
    }

    @Override
    public void saveQuizSession(QuizSession quizSession) {
        try {
            String redisKey = "quiz-session:" + quizSession.getAccountId() + ":" + quizSession.getQuizId();
            redisService.updateData(redisKey, quizSession, quizSession.getTestDuration() + 10, TimeUnit.MINUTES);
        } catch (Exception e) {
            throw new ApiException(ErrorCode.INTERNAL_SERVER_ERROR.getStatusCode().value(),
                    "Error saving quiz session: " + e.getMessage());
        }
    }

    @Override
    public void updateQuizAnswer(Long userId, Long quizId, Integer questionId, String answer) {
        try {
            QuizSession quizSession = getQuizSession(userId, quizId);
            Map<Integer, String> answers = quizSession.getAnswers();
            answers.put(questionId, answer);
            quizSession.setAnswers(answers);
            saveQuizSession(quizSession);
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException(ErrorCode.INTERNAL_SERVER_ERROR.getStatusCode().value(),
                    "Error updating quiz answer: " + e.getMessage());
        }
    }

    @Override
    public List<AnalyzeData> getListDataAnalyze(List<Long> accountIds) throws Exception {
        try{
            LocalDateTime now= LocalDateTime.now();
            LocalDateTime startDate= now.minusDays(7);
            List<Grade> grades= gradeRepository.findByAccountIdsAndCreatedAtBetween(accountIds,startDate,now);
            Map<Long, List<Grade>> gradeMaps= grades.stream()
                    .collect(Collectors.groupingBy(Grade::getAccountId));
            List<AnalyzeData> analyzeData= new ArrayList<>();
            for(Map.Entry<Long,List<Grade>> entry : gradeMaps.entrySet()){
                float assignmentGrade= 0;
                int numberOfAssignment=0;
                int numberOfExam=0;
                float examGrade= 0;
                for(Grade grade : entry.getValue()){
                    if(grade.getAssignment()!=null){
                        numberOfAssignment++;
                        assignmentGrade += grade.getScore();
                    }
                    if(grade.getQuiz() != null){
                        numberOfExam++;
                        examGrade += grade.getScore();
                    }
                }
                if(numberOfAssignment!=0 || numberOfExam!=0){
                    analyzeData.add(AnalyzeData.builder()
                            .onlineCourseComplete(numberOfAssignment)
                            .onlineTestComplete(numberOfExam)
                            .assignmentScore(numberOfAssignment==0?0:(assignmentGrade/numberOfAssignment))
                            .examScore(numberOfExam==0?0:(examGrade/numberOfExam))
                            .build());
                }
            }
            return analyzeData;
        }catch (Exception e){
            throw new Exception(e.getMessage());
        }
    }

    @Override
    public AnalyzeData getAnalyzeDataByAccountId(Long accountId) throws Exception {
        try{
            List<Grade> grades= gradeRepository.findByAccountId(accountId);
            float assignmentGrade= 0;
            int numberOfAssignment=0;
            int numberOfExam=0;
            float examGrade= 0;
            for(Grade grade : grades){
                if(grade.getAssignment()!=null){
                    numberOfAssignment++;
                    assignmentGrade += grade.getScore();
                }
                if(grade.getQuiz() != null){
                    numberOfExam++;
                    examGrade += grade.getScore();
                }
            }
            if(numberOfAssignment!=0 || numberOfExam!=0){
                return AnalyzeData.builder()
                        .onlineCourseComplete(numberOfAssignment)
                        .onlineTestComplete(numberOfExam)
                        .assignmentScore(numberOfAssignment==0?0:(assignmentGrade/numberOfAssignment))
                        .examScore(numberOfExam==0?0:(examGrade/numberOfExam))
                        .build();
            }else{
                return null;
            }
        }catch (Exception e){
            throw new Exception(e);
        }
    }


    private void updateExamScore(Long userId, Assessment assessment) {
        try {
            List<Grade> quizGrades = gradeRepository.findByUserIdAndQuizId(userId, null);
            List<Grade> assignmentGrades = gradeRepository.findByUserIdAndAssignmentId(userId, null);

            List<Long> quizIds = quizGrades.stream()
                    .filter(grade -> grade.getQuiz() != null)
                    .map(grade -> grade.getQuiz().getId())
                    .distinct()
                    .collect(Collectors.toList());

            List<Quiz> quizzes = quizRepository.findAllById(quizIds);
            Map<Long, Quiz> quizMap = quizzes.stream()
                    .collect(Collectors.toMap(Quiz::getId, quiz -> quiz));

            float totalScore = 0;
            int count = 0;

            for (Grade grade : quizGrades) {
                if (grade.getQuiz() != null) { // Sửa từ getQuizId() thành getQuiz()
                    Quiz quiz = quizMap.get(grade.getQuiz().getId());
                    if (quiz != null && assessment.getId().equals(quiz.getAssessment().getId())) {
                        totalScore += grade.getScore();
                        count++;
                    }
                }
            }

            for (Grade grade : assignmentGrades) {
                if (grade.getAssignment() != null) {
                    totalScore += grade.getScore();
                    count++;
                }
            }

            if (count > 0) {
                assessment.setExamScore(Math.round(totalScore / count));
                assessmentRepository.save(assessment);
            }
        } catch (Exception e) {
            throw new ApiException(ErrorCode.INTERNAL_SERVER_ERROR.getStatusCode().value(),
                    "Error updating exam score: " + e.getMessage());
        }
    }

    @Getter
    private static class DocumentRequest {
        private final Long documentId;

        public DocumentRequest(Long documentId) {
            this.documentId = documentId;
        }

    }
}