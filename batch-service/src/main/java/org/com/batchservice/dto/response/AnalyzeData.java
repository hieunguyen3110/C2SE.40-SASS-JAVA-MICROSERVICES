package org.com.batchservice.dto.response;

import lombok.*;
import org.com.batchservice.dto.request.AssignmentCompletionRequest;
import org.com.batchservice.dto.request.CoursePeriodRequest;
import org.com.batchservice.dto.request.TestRequest;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AnalyzeData {
    private List<AssignmentCompletionRequest> assignment_completion_rate;
    private List<TestRequest> exam_score;
    private Integer online_courses_completed;
    private List<CoursePeriodRequest> course_period;
    private Long accountId;
}
