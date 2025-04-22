package org.com.elearningservice.service.impl;

import lombok.RequiredArgsConstructor;
import org.com.elearningservice.dto.response.AnalyzeData;
import org.com.elearningservice.entity.Grade;
import org.com.elearningservice.enums.ResultType;
import org.com.elearningservice.repository.GradeRepository;
import org.com.elearningservice.service.ELearningService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ELearningServiceImpl implements ELearningService {
    private final GradeRepository gradeRepository;

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
        }catch (Exception e){
            throw new Exception(e);
        }
    }
}