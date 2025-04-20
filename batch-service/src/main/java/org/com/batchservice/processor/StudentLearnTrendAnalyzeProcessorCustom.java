package org.com.batchservice.processor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.com.batchservice.dto.request.AnalyzeRequest;
import org.com.batchservice.dto.response.AnalyzeData;
import org.com.batchservice.dto.response.MessageAnalyze;
import org.com.batchservice.repository.RecommendationClient;
import org.com.batchservice.repository.StudyGroupClient;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class StudentLearnTrendAnalyzeProcessorCustom implements ItemProcessor<AnalyzeData, MessageAnalyze> {
    private final RecommendationClient recommendationClient;
    private final StudyGroupClient studyGroupClient;
    @Override
    public MessageAnalyze process(AnalyzeData item) throws Exception {
        try{
            Boolean checkParticipate= studyGroupClient.checkAccountIsParticipateGroup(item.getAccountId()).getData();
            AnalyzeRequest request= AnalyzeRequest.builder()
                    .Online_Courses_Completed(List.of(item.getOnlineCourseComplete()+ item.getOnlineTestComplete()))
                    .Participation_in_Discussions(List.of(checkParticipate?"Yes":"No"))
                    .Assignment_Completion_Rate(List.of(item.getAssignmentScore()))
                    .Exam_Score(List.of(item.getExamScore()))
                    .build();
            String response= recommendationClient.predictStudentLearningTrending(request).getData();
            return MessageAnalyze.builder()
                    .accountId(item.getAccountId())
                    .message(response)
                    .build();
        }catch (Exception e){
            log.error("Error when try get solution:" + e);
            throw new Exception(e);
        }
    }
}
