package org.com.batchservice.processor;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.com.batchservice.dto.request.AnalyzeRequest;
import org.com.batchservice.dto.request.AssignmentCompletionRequest;
import org.com.batchservice.dto.request.AssignmentGradeRequest;
import org.com.batchservice.dto.response.AnalyzeData;
import org.com.batchservice.dto.response.DocumentDto;
import org.com.batchservice.dto.response.LearningAnalyzeResponse;
import org.com.batchservice.dto.response.MessageAnalyze;
import org.com.batchservice.repository.DocumentClient;
import org.com.batchservice.repository.RecommendationClient;
import org.com.batchservice.repository.StudyGroupClient;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Component
@Slf4j
@RequiredArgsConstructor
public class StudentLearnTrendAnalyzeProcessorCustom implements ItemProcessor<AnalyzeData, MessageAnalyze> {
    private final RecommendationClient recommendationClient;
    private final StudyGroupClient studyGroupClient;
    private final DocumentClient documentClient;

    private List<DocumentDto> getRandomElements(List<DocumentDto> l) {
        Random r = new Random();
        List<DocumentDto> newList = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            int randomIndex = r.nextInt(l.size());
            newList.add(l.get(randomIndex));
        }

        return newList;
    }
    @Override
    public MessageAnalyze process(AnalyzeData item) throws Exception {
        try{
            float weakenThreshold = 65.0F;
            Boolean checkParticipate= studyGroupClient.checkAccountIsParticipateGroup(item.getAccountId()).getData();
            AnalyzeRequest request= AnalyzeRequest.builder()
                    .online_courses_completed(List.of(item.getOnline_courses_completed()))
                    .participation_in_discussions(List.of(checkParticipate?"Yes":"No"))
                    .exam_score(item.getExam_score())
                    .assignment_completion_rate(item.getAssignment_completion_rate())
                    .course_period(item.getCourse_period())
                    .build();
            LearningAnalyzeResponse response;
            response = recommendationClient.predictStudentLearningTrending(request).getData();
            if(response == null){
                response = new LearningAnalyzeResponse();
                response.setDocument_read_again(new ArrayList<>());
                response.setDocument_recommend(new ArrayList<>());
                response.setGeneral_assessment(null);
                response.setProgress_tracking(null);
                response.setWeekly_study_plan(null);
                response.setSubject_weakens(new ArrayList<>());
                response.setImprovement_suggestions(new ArrayList<>());
            }else{
                List<AssignmentCompletionRequest> subjectWeakens = response.getSubject_weakens();
                List<Long> subjectIds = subjectWeakens.stream().map(AssignmentCompletionRequest::getSubject_id).toList();
                List<DocumentDto> documentDtos = documentClient.getAllDocumentsBySubjectIds(subjectIds).getData();
                List<Long> docIds = new ArrayList<>();
                response.getSubject_weakens().forEach(assignRequest -> {
                    List<AssignmentGradeRequest> assignGradeReqs = assignRequest.getAssigment_grades();
                    if(!assignGradeReqs.isEmpty()){
                        for(AssignmentGradeRequest assignGradeReq : assignGradeReqs){
                            if(assignGradeReq.getGrade() <= weakenThreshold){
                                docIds.add(assignGradeReq.getDoc_id());
                            }
                        }
                    }
                });
                List<DocumentDto> documentNeedReadAgain = documentDtos.stream()
                        .filter(document -> docIds.contains(document.getDocId()))
                        .toList();
                List<DocumentDto> documentRecommend = new ArrayList<>();
                for(DocumentDto documentDto : documentDtos){
                    if(!documentNeedReadAgain.contains(documentDto)){
                        documentRecommend.add(documentDto);
                    }
                }
                if(documentRecommend.size()<=5){
                    response.setDocument_recommend(documentRecommend);
                }else{
                    response.setDocument_recommend(getRandomElements(documentRecommend));
                }
                response.setDocument_read_again(documentNeedReadAgain);
            }

            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());
            objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            String messageJson = objectMapper.writeValueAsString(response);
            return MessageAnalyze.builder()
                    .accountId(item.getAccountId())
                    .message(messageJson)
                    .build();
        }catch (Exception e){
            log.error("Error when try get solution:" + e);
            throw new Exception(e);
        }
    }
}
