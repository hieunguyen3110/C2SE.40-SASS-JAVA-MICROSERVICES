package org.com.elearningservice.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.com.elearningservice.dto.response.AccountDto;
import org.com.elearningservice.dto.response.QuestionDTO;
import org.com.elearningservice.dto.response.QuizSessionDTO;
import org.com.elearningservice.dto.response.SubjectDTO;
import org.com.elearningservice.entity.Grade;
import org.com.elearningservice.entity.GradeQuestion;
import org.com.elearningservice.entity.Question;
import org.com.elearningservice.enums.ResultType;
import org.com.elearningservice.exception.ApiException;
import org.com.elearningservice.repository.GradeRepository;
import org.com.elearningservice.repository.QuestionRepository;
import org.com.elearningservice.service.QuizService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

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
    private final ObjectMapper objectMapper;

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
        try {
            String subjectsKey = "subjects";
            Object subjectsObj = redisTemplate.opsForValue().get(subjectsKey);
            if (subjectsObj == null) {
                throw new ApiException(HttpStatus.NOT_FOUND.value(), "Subjects not found in Redis");
            }

            return objectMapper.convertValue(subjectsObj, new TypeReference<List<SubjectDTO>>() {});
        } catch (Exception e) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Error deserializing subjects from Redis: " + e.getMessage());
        }
    }

    public void validateSubjectId(Long subjectId) {
        try {
            List<SubjectDTO> subjects = getSubjectsFromRedis();
            boolean subjectExists = subjects.stream().anyMatch(subject -> subject.getId().equals(subjectId));
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
            redisTemplate.opsForValue().set(sessionKey, session, (duration+5) * 60L , TimeUnit.SECONDS);

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
            QuizSessionDTO session = (QuizSessionDTO) redisTemplate.opsForValue().get(sessionKey);
            return session != null ? session : startSession(null, 0, 0, isAssignment);
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Error restoring session: " + e.getMessage());
        }
    }

    @Override
    public Grade submitSession(Long subjectId, List<String> userAnswers, boolean isAssignment) {
        try {
            Long accountId = getCurrentAccountId();
            validateSubjectId(subjectId);

            String sessionKey = "session:" + accountId + ":" + (isAssignment ? "assignment" : "quiz");
            QuizSessionDTO session = (QuizSessionDTO) redisTemplate.opsForValue().get(sessionKey);
            if (session == null) {
                throw new ApiException(HttpStatus.NOT_FOUND.value(), "Session not found");
            }

            float score = 0;
            for (int i = 0; i < userAnswers.size(); i++) {
                if (userAnswers.get(i).equals(session.getQuestions().get(i).getCorrectAnswer())) {
                    score++;
                }
            }

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
                    try {
                        question.setOptions(objectMapper.writeValueAsString(questionDTO.getOptions()));
                    } catch (Exception e) {
                        throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Error serializing options: " + e.getMessage());
                    }
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
            gradeRepository.save(result);

            redisTemplate.delete(sessionKey);

            return result;
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Error submitting session: " + e.getMessage());
        }
    }

    @Override
    public QuizSessionDTO updateSessionAnswer(List<String> userAnswers, boolean isAssignment) {
        try {
            Long accountId = getCurrentAccountId();
            String sessionKey = "session:" + accountId + ":" + (isAssignment ? "assignment" : "quiz");
            QuizSessionDTO session = (QuizSessionDTO) redisTemplate.opsForValue().get(sessionKey);
            if (session == null) {
                throw new ApiException(HttpStatus.NOT_FOUND.value(), "Session not found");
            }

            session.setUserAnswers(userAnswers);

            long remainingTtl = redisTemplate.getExpire(sessionKey, TimeUnit.SECONDS);

            if(remainingTtl <= 0) {
                throw new ApiException(HttpStatus.BAD_REQUEST.value(), "Session has expired");
            }

            redisTemplate.opsForValue().set(sessionKey, session, remainingTtl, TimeUnit.SECONDS);

            return session;
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Error updating session answer: " + e.getMessage());
        }
    }

    @Override
    public List<Grade> getHistory() {
        try {
            Long accountId = getCurrentAccountId();
            return gradeRepository.findByAccountId(accountId);
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Error retrieving history: " + e.getMessage());
        }
    }
}