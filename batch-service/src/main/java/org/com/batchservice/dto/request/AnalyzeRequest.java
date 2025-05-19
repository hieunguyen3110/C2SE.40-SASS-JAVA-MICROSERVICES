package org.com.batchservice.dto.request;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnalyzeRequest {
    private List<Integer> online_courses_completed;
    private List<String> participation_in_discussions;
    private List<AssignmentCompletionRequest> assignment_completion_rate;
    private List<TestRequest> exam_score;
    private List<CoursePeriodRequest> course_period;
}
