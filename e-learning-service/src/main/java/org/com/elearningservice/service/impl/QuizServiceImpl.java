package org.com.elearningservice.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.com.elearningservice.dto.response.*;
import org.com.elearningservice.entity.Grade;
import org.com.elearningservice.entity.GradeQuestion;
import org.com.elearningservice.entity.Question;
import org.com.elearningservice.enums.ResultType;
import org.com.elearningservice.exception.ApiException;
import org.com.elearningservice.repository.GradeRepository;
import org.com.elearningservice.repository.QuestionRepository;
import org.com.elearningservice.repository.http.DocumentClient;
import org.com.elearningservice.service.QuizService;
import org.com.elearningservice.service.RedisService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuizServiceImpl implements QuizService {

    private final RestTemplate restTemplate;
    private final QuestionRepository questionRepository;
    private final GradeRepository gradeRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final RedisService redisService;
    private final ObjectMapper objectMapper;
    private final DocumentClient documentClient;

    @Value("${app.ai-service.url}")
    private String aiServiceUrl;

    private Long getCurrentAccountId() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            AccountDto accountDto = (AccountDto) authentication.getPrincipal();
            return accountDto.getAccountId();
        } catch (Exception e) {
            throw new ApiException(HttpStatus.UNAUTHORIZED.value(), "User not authenticated");
        }
    }

    @Override
    public List<SubjectDTO> getSubjectsFromRedis() {
        String subjectsKey = "subject";
        try {
            String json = (String) redisService.getData(subjectsKey);
            if (json != null) {
                return objectMapper.readValue(
                        json,
                        new TypeReference<>() {}
                );
            }

            List<SubjectDTO> subjects = documentClient.getAllSubject().getData();

            if (subjects == null || subjects.isEmpty()) {
                throw new ApiException(HttpStatus.NOT_FOUND.value(), "No subjects found from document-service");
            }

            redisTemplate.opsForValue().set(subjectsKey, subjects, Duration.ofHours(1));

            return subjects;
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Error retrieving subjects: " + e.getMessage());
        }
    }


    public void validateSubjectId(Long subjectId) {
        try {
            List<SubjectDTO> subjects = getSubjectsFromRedis();
            boolean subjectExists = subjects.stream().anyMatch(subject -> subject.getSubjectId().equals(subjectId));
            if (!subjectExists) {
                throw new ApiException(HttpStatus.BAD_REQUEST.value(), "Invalid subjectId: " + subjectId);
            }
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Error validating subjectId: " + e.getMessage());
        }
    }

    private List<String> getDocIdsFromRedis(Long subjectId) {
        try {
            String docKey = "subject-docs:" + subjectId;
            Object docIdsObj = redisTemplate.opsForValue().get(docKey);
            if (docIdsObj == null) {
                throw new ApiException(HttpStatus.NOT_FOUND.value(), "Document IDs not found in Redis for subjectId: " + subjectId);
            }

            return objectMapper.convertValue(docIdsObj, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Error deserializing docIds from Redis: " + e.getMessage());
        }
    }

    private List<QuestionDTO> callAiService(List<String> docIds, int numberOfQuestions) {
        try {
            String docIdsParam = String.join(",", docIds);
            String url = aiServiceUrl + "/document/generate-question?docIds=" + docIdsParam;

            QuestionDTO[] response = restTemplate.getForObject(url, QuestionDTO[].class);
            if (response == null) {
                throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE.value(), "AI service returned null response");
            }

            List<QuestionDTO> questions = Arrays.asList(response);
            if (questions.size() < numberOfQuestions) {
                throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE.value(),
                        "AI service returned fewer questions than requested: " + questions.size() + " < " + numberOfQuestions);
            }

            return questions.stream().limit(numberOfQuestions).collect(Collectors.toList());
        } catch (Exception e) {
            throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE.value(), "Error calling AI service: " + e.getMessage());
        }
    }


    private List<QuestionDTO> generateQuizQuestions(Long subjectId, int numberOfQuestions) {
        try {
            validateSubjectId(subjectId);

            List<Question> existingQuestions = questionRepository.findBySubjectId(subjectId);
            if (existingQuestions.size() >= numberOfQuestions) {
                Collections.shuffle(existingQuestions);
                return existingQuestions.stream()
                        .limit(numberOfQuestions)
                        .map(this::convertToQuestionDTO)
                        .collect(Collectors.toList());
            }

            int questionsToGenerate = numberOfQuestions - existingQuestions.size();
            List<String> docIds = getDocIdsFromRedis(subjectId);
            List<QuestionDTO> newQuestions = callAiService(docIds, questionsToGenerate);

            for (QuestionDTO dto : newQuestions) {
                Question question = new Question();
                question.setSubjectId(subjectId);
                question.setQuestionText(dto.getQuestion());
                question.setCorrectAnswer(dto.getCorrectAnswer());
                try {
                    question.setOptions(dto.getOptions());
                } catch (Exception e) {
                    throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Error serializing options: " + e.getMessage());
                }
                questionRepository.save(question);
            }

            List<QuestionDTO> allQuestions = existingQuestions.stream()
                    .map(this::convertToQuestionDTO).collect(Collectors.toList());
            allQuestions.addAll(newQuestions);
            Collections.shuffle(allQuestions);
            return allQuestions.stream().limit(numberOfQuestions).collect(Collectors.toList());
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Error generating quiz questions: " + e.getMessage());
        }
    }

    private List<QuestionDTO> generateAssignmentQuestions(Long subjectId, int numberOfQuestions) {
        try {
            validateSubjectId(subjectId);
            List<String> docIds = getDocIdsFromRedis(subjectId);
            return callAiService(docIds, numberOfQuestions);
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Error generating assignment questions: " + e.getMessage());
        }
    }

    private QuestionDTO convertToQuestionDTO(Question question) {
        try {
            QuestionDTO dto = new QuestionDTO();
            dto.setQuestion(question.getQuestionText());
            dto.setCorrectAnswer(question.getCorrectAnswer());
            dto.setOptions(question.getOptions());
            return dto;
        } catch (Exception e) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Error deserializing options: " + e.getMessage());
        }
    }

    @Override
    public QuizSessionDTO startSession(Long subjectId, int numberOfQuestions, int duration, boolean isAssignment) {
        try {
            if (duration <= 0) {
                throw new ApiException(HttpStatus.BAD_REQUEST.value(), "Duration must be greater than 0");
            }

            Long accountId = getCurrentAccountId();

            List<QuestionDTO> questions;
            if (isAssignment) {
                questions = generateAssignmentQuestions(subjectId, numberOfQuestions);
            } else {
                questions = generateQuizQuestions(subjectId, numberOfQuestions);
            }

            QuizSessionDTO session = new QuizSessionDTO();
            session.setAccountId(accountId);
            session.setSubjectId(subjectId);
            session.setQuestions(questions);
            session.setUserAnswers(new ArrayList<>());
            session.setAssignment(isAssignment);

            String sessionKey = "session:" + accountId + ":" + (isAssignment ? "assignment" : "quiz");
            redisService.saveData(sessionKey, session, (duration+5) * 60L);

            return session;
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Error starting session: " + e.getMessage());
        }
    }

    @Override
    public QuizSessionDTO restoreSession(boolean isAssignment) {
        try {
            Long accountId = getCurrentAccountId();
            String sessionKey = "session:" + accountId + ":" + (isAssignment ? "assignment" : "quiz");
            QuizSessionDTO session = (QuizSessionDTO) redisService.getData(sessionKey);
            return session != null ? session : startSession(null, 0, 0, isAssignment);
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Error restoring session: " + e.getMessage());
        }
    }

    @Override
    public GradeDto submitSession(Long subjectId, List<String> userAnswers, boolean isAssignment) {
        try {
            Long accountId = getCurrentAccountId();
            validateSubjectId(subjectId);

            String sessionKey = "session:" + accountId + ":" + (isAssignment ? "assignment" : "quiz");
            QuizSessionDTO session = (QuizSessionDTO) redisService.getData(sessionKey);
            if (session == null) {
                throw new ApiException(HttpStatus.NOT_FOUND.value(), "Session not found");
            }

            int correctCount = 0;
            int totalQuestions = session.getQuestions().size();

            for (int i = 0; i < totalQuestions; i++) {
                String userAnswer = (i < userAnswers.size()) ? userAnswers.get(i) : null;
                String correctAnswer = session.getQuestions().get(i).getCorrectAnswer();
                if (userAnswer != null && userAnswer.equals(correctAnswer)) {
                    correctCount++;
                }
            }

            float score = ((float) correctCount / totalQuestions) * 100;

            Grade result = new Grade();
            result.setAccountId(accountId);
            result.setSubjectId(subjectId);
            result.setScore(score);
            result.setTotalQuestions(userAnswers.size());
            result.setCreatedAt(LocalDateTime.now());
            result.setType(isAssignment ? ResultType.ASSIGNMENT : ResultType.QUIZ);

            List<GradeQuestion> gradeQuestions = new ArrayList<>();
            for (int i = 0; i < session.getQuestions().size(); i++) {
                QuestionDTO questionDTO = session.getQuestions().get(i);

                Question question;
                if (isAssignment) {
                    question = new Question();
                    question.setSubjectId(subjectId);
                    question.setQuestionText(questionDTO.getQuestion());
                    question.setCorrectAnswer(questionDTO.getCorrectAnswer());
                    question.setOptions(questionDTO.getOptions());
                    question = questionRepository.save(question);
                } else {
                    // Nếu là quiz, câu hỏi đã tồn tại trong DB
                    question = questionRepository.findByQuestionTextAndSubjectId(questionDTO.getQuestion(), subjectId)
                            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND.value(), "Question not found in DB"));
                }

                GradeQuestion gradeQuestion = new GradeQuestion();
                gradeQuestion.setGrade(result);
                gradeQuestion.setQuestion(question);
                gradeQuestion.setUserAnswer(userAnswers.get(i));
                gradeQuestions.add(gradeQuestion);
            }

            result.setGradeQuestions(gradeQuestions);
            Grade saveGrade= gradeRepository.save(result);
            List<GradeQuestionResponse> gradeQuestionResponses= saveGrade.getGradeQuestions().stream()
                            .map(gradeQuestion -> GradeQuestionResponse.builder()
                                    .question(QuestionDTO.builder()
                                            .question(gradeQuestion.getQuestion().getQuestionText())
                                            .correctAnswer(gradeQuestion.getQuestion().getCorrectAnswer())
                                            .options(gradeQuestion.getQuestion().getOptions())
                                            .build())
                                    .userAnswer(gradeQuestion.getUserAnswer())
                                    .build())
                            .toList();
            redisService.deleteData(sessionKey);
            return GradeDto.builder()
                    .id(saveGrade.getId())
                    .score(saveGrade.getScore())
                    .totalQuestions(saveGrade.getTotalQuestions())
                    .subjectId(saveGrade.getSubjectId())
                    .type(saveGrade.getType().name())
                    .gradeQuestions(gradeQuestionResponses)
                    .build();
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Error submitting session: " + e.getMessage());
        }
    }

    @Override
    public QuizSessionDTO updateSessionAnswer(int index, String userAnswer, boolean isAssignment) {
        try {
            Long accountId = getCurrentAccountId();
            String sessionKey = "session:" + accountId + ":" + (isAssignment ? "assignment" : "quiz");
            QuizSessionDTO session = (QuizSessionDTO) redisService.getData(sessionKey);

            if (session == null) {
                throw new ApiException(HttpStatus.NOT_FOUND.value(), "Session not found");
            }

            while (session.getUserAnswers().size() <= index) {
                session.getUserAnswers().add("");
            }

            session.getUserAnswers().set(index, userAnswer);

            long remainingTtl = redisTemplate.getExpire(sessionKey, TimeUnit.SECONDS);
            if (remainingTtl <= 0) {
                throw new ApiException(HttpStatus.BAD_REQUEST.value(), "Session has expired");
            }

            redisService.saveData(sessionKey, session, remainingTtl);

            return session;
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Error updating session answer: " + e.getMessage());
        }
    }


    @Override
    public List<GradeDto> getHistory() {
        try {
            Long accountId = getCurrentAccountId();
            List<Grade> grades= gradeRepository.findByAccountId(accountId);
            List<SubjectDTO> subjectDTOS = getSubjectsFromRedis();
            List<GradeDto> gradeDtos= new ArrayList<>();
            for(Grade grade : grades){
                List<GradeQuestionResponse> gradeQuestionResponse= grade.getGradeQuestions().stream()
                        .map(gradeQuestion -> GradeQuestionResponse.builder()
                                .userAnswer(gradeQuestion.getUserAnswer())
                                .question(QuestionDTO.builder()
                                        .options(gradeQuestion.getQuestion().getOptions())
                                        .question(gradeQuestion.getQuestion().getQuestionText())
                                        .correctAnswer(gradeQuestion.getQuestion().getCorrectAnswer())
                                        .build())
                                .build())
                        .toList();
                GradeDto gradeDto= GradeDto.builder()
                        .id(grade.getId())
                        .score(grade.getScore())
                        .totalQuestions(grade.getTotalQuestions())
                        .subjectId(grade.getSubjectId())
                        .type(grade.getType().name())
                        .gradeQuestions(gradeQuestionResponse)
                        .createdAt(grade.getCreatedAt().toString())
                        .subjectName(subjectDTOS.stream().filter(subjectDTO -> subjectDTO.getSubjectId().equals(grade.getSubjectId())).toList().get(0).getSubjectName())
                        .build();
                gradeDtos.add(gradeDto);
            }
            return gradeDtos;
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Error retrieving history: " + e.getMessage());
        }
    }
}