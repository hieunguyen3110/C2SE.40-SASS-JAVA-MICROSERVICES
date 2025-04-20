package org.com.identityservice.dto.request;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnalyzeRequest {
    private List<Integer> Online_Courses_Completed;
    private List<String> Participation_in_Discussions;
    private List<Float> Assignment_Completion_Rate;
    private List<Float> Exam_Score;
}
