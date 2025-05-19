package org.com.elearningservice.dto.request;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AssignmentCompletionRequest {
    private Long subject_id;
    private String subject_name;
    private List<AssignmentGradeRequest> assigment_grades;
    private Float avg_score;
}
