package org.com.elearningservice.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.com.elearningservice.dto.request.AssignmentCompletionRequest;
import org.com.elearningservice.dto.request.AssignmentGradeRequest;
import org.com.elearningservice.dto.request.CoursePeriodRequest;
import org.com.elearningservice.dto.request.SubjectTestRequest;
import org.com.elearningservice.dto.response.AnalyzeData;
import org.com.elearningservice.dto.response.AnalyzeDataResponse;
import org.com.elearningservice.dto.response.QuestionDTO;
import org.com.elearningservice.dto.response.SubjectDTO;
import org.com.elearningservice.entity.CoursePeriod;
import org.com.elearningservice.entity.Grade;
import org.com.elearningservice.entity.Question;
import org.com.elearningservice.enums.ResultType;
import org.com.elearningservice.repository.CoursePeriodRepository;
import org.com.elearningservice.repository.GradeRepository;
import org.com.elearningservice.repository.QuestionRepository;
import org.com.elearningservice.repository.http.DocumentClient;
import org.com.elearningservice.service.ELearningService;
import org.com.elearningservice.service.RedisService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ELearningServiceImpl implements ELearningService {
    private final GradeRepository gradeRepository;
    private final QuestionRepository questionRepository;
    private final CoursePeriodRepository coursePeriodRepository;
    private final RedisService redisService;
    private final ObjectMapper objectMapper;
    private final DocumentClient documentClientBatch;

    @Override
    public List<AnalyzeDataResponse> getListDataAnalyze(List<Long> accountIds) throws Exception {
        try{
            LocalDateTime now= LocalDateTime.now();
            LocalDateTime startDate= now.minusDays(7);
            List<CoursePeriod> coursePeriods = coursePeriodRepository.findByAccountIdsAndStartDateAndEndDate(accountIds,now);
            List<Grade> grades= gradeRepository.findByAccountIdsAndCreatedAtBetween(accountIds,startDate,now);
            List<Grade> gradesFilter = new ArrayList<>();
            for(Grade grade : grades){
                for(CoursePeriod coursePeriod : coursePeriods){
                    String subjectName = coursePeriod.getSubjects().get(grade.getSubjectId());
                    if(subjectName != null && grade.getAccountId().equals(coursePeriod.getAccountId())){
                        gradesFilter.add(grade);
                        break;
                    }
                }
            }
            String subjectKey = "subject";
            String subjectJson = (String) redisService.getData(subjectKey);
            List<SubjectDTO> subjectDTOS;
            if(subjectJson != null){
                subjectDTOS = objectMapper.readValue(subjectJson, new TypeReference<>(){});
            }else{
                subjectDTOS = documentClientBatch.getAllSubject().getData();
            }
            Map<Long, String> subjectMap = subjectDTOS.stream()
                    .collect(Collectors.toMap(SubjectDTO::getSubjectId, SubjectDTO::getSubjectName));
            Map<Long, List<Grade>> gradeMaps= gradesFilter.stream()
                    .collect(Collectors.groupingBy(Grade::getAccountId));
            List<AnalyzeDataResponse> analyzeData= new ArrayList<>();
            for(Map.Entry<Long,List<Grade>> entry : gradeMaps.entrySet()){
                Long accountId = entry.getKey();
                List<Grade> gradeQuiz = entry.getValue().stream()
                        .filter(grade -> grade.getType().equals(ResultType.QUIZ))
                        .toList();
                List<Grade> gradeAssignment = entry.getValue().stream()
                        .filter(grade -> grade.getType().equals(ResultType.ASSIGNMENT))
                        .toList();
                Map<Long,  SubjectTestRequest> subjectTestMap = new HashMap<>();
                Map<Long, AssignmentCompletionRequest> assignmentMap = new HashMap<>();
                Map<Long, Float> totalTestScores = new HashMap<>();
                Map<Long, Integer> countTestScores = new HashMap<>();
                for(Grade grade : gradeQuiz){
                    Long subjectId = grade.getSubjectId();
                    String subjectName = subjectMap.get(grade.getSubjectId());
                    SubjectTestRequest isExist = subjectTestMap.get(grade.getSubjectId());
                    totalTestScores.put(subjectId, totalTestScores.getOrDefault(subjectId,0f) + grade.getScore());
                    countTestScores.put(subjectId, countTestScores.getOrDefault(subjectId,0)+1);
                    if(isExist == null){
                        SubjectTestRequest subjectTestRequest = SubjectTestRequest.builder()
                                .subject_name(subjectName)
                                .subject_id(grade.getSubjectId())
                                .build();
                        subjectTestMap.put(grade.getSubjectId(),subjectTestRequest);
                    }
                }
                for(Map.Entry<Long,SubjectTestRequest> entry1 : subjectTestMap.entrySet()){
                    Long subjectId = entry1.getKey();
                    float totalScore = totalTestScores.get(subjectId);
                    int countScore = countTestScores.get(subjectId);
                    entry1.getValue().setAvg_score(totalScore/countScore);
                }
                for(Grade grade : gradeAssignment){
                    Long subjectId = grade.getSubjectId();
                    String subjectName = subjectMap.get(subjectId);
                    AssignmentCompletionRequest isExist = assignmentMap.get(grade.getSubjectId());
                    if(isExist == null){
                        AssignmentCompletionRequest request = AssignmentCompletionRequest.builder()
                                .subject_id(subjectId)
                                .subject_name(subjectName)
                                .assigment_grades(new ArrayList<>())
                                .build();
                        request.getAssigment_grades().add(AssignmentGradeRequest.builder()
                                        .docId(grade.getDocId())
                                        .grade(grade.getScore())
                                .build());
                        assignmentMap.put(subjectId,request);
                    }else{
                        isExist.getAssigment_grades().add(AssignmentGradeRequest.builder()
                                .docId(grade.getDocId())
                                .grade(grade.getScore())
                                .build());
                    }
                }
                for(Map.Entry<Long,AssignmentCompletionRequest> entry1 : assignmentMap.entrySet()){
                    AssignmentCompletionRequest assignmentGrade = entry1.getValue();
                    double avg_score = assignmentGrade.getAssigment_grades().stream().mapToDouble(AssignmentGradeRequest::getGrade).average().orElse(0);
                    entry1.getValue().setAvg_score((float) avg_score);
                }
                CoursePeriod coursePeriod = coursePeriods.stream().filter(coursePeriod1 -> coursePeriod1.getAccountId().equals(accountId)).toList().get(0);
                Map<Long, String> subjectCourseMaps = coursePeriod.getSubjects();
                List<CoursePeriodRequest> coursePeriodRequests = new ArrayList<>();
                for(Map.Entry<Long, String> subject : subjectCourseMaps.entrySet()){
                    coursePeriodRequests.add(CoursePeriodRequest.builder()
                                    .subject_id(subject.getKey())
                                    .subject_name(subject.getValue())
                            .build());
                }
                AnalyzeDataResponse response = AnalyzeDataResponse.builder()
                        .assignment_completion_rate(new ArrayList<>(assignmentMap.values()))
                        .exam_score(new ArrayList<>(subjectTestMap.values()))
                        .online_courses_completed(gradeQuiz.size()+gradeAssignment.size())
                        .course_period(coursePeriodRequests)
                        .accountId(accountId)
                        .build();
                analyzeData.add(response);
            }
            return analyzeData;
        }catch (Exception e){
            throw new Exception(e.getMessage());
        }
    }

    @Override
    public AnalyzeData getAnalyzeDataByAccountId(Long accountId) throws Exception {
        try{
            LocalDateTime now= LocalDateTime.now();
            if(now.getDayOfWeek().getValue() == 1){
                List<Grade> grades= gradeRepository.findByAccountId(accountId);
                float assignmentGrade= 0;
                int numberOfAssignment=0;
                int numberOfExam=0;
                float examGrade= 0;
                for(Grade grade : grades){
                    if(grade.getType().equals(ResultType.ASSIGNMENT)){
                        numberOfAssignment++;
                        assignmentGrade += grade.getScore();
                    }
                    if(grade.getType().equals(ResultType.QUIZ)){
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
            }else{
                return null;
            }
        }catch (Exception e){
            throw new Exception(e);
        }
    }

    @Override
    public String saveQuestion(List<QuestionDTO> questionDTOS, Long subjectId) throws Exception {
        try{
            List<Question> saveAllQuestion= new ArrayList<>();
            for(QuestionDTO questionDTO: questionDTOS){
                Question question= Question.builder()
                        .questionText(questionDTO.getQuestion())
                        .correctAnswer(questionDTO.getCorrectAnswer())
                        .subjectId(subjectId)
                        .options(questionDTO.getOptions())
                        .build();
                saveAllQuestion.add(question);
            }
            questionRepository.saveAll(saveAllQuestion);
            return "Save all question successful";
        }catch (Exception e){
            throw new Exception(e);
        }
    }
}