package org.com.elearningservice.dto.response;

import lombok.*;
import org.com.elearningservice.dto.request.AssignmentCompletionRequest;
import org.com.elearningservice.dto.request.CoursePeriodRequest;
import org.com.elearningservice.dto.request.SubjectTestRequest;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AnalyzeDataResponse {
    private List<AssignmentCompletionRequest> assignment_completion_rate;
    private List<SubjectTestRequest> exam_score;
    private Integer online_courses_completed;
    private List<CoursePeriodRequest> course_period;
    private Long accountId;
}
