package org.com.batchservice.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class AssignmentCompletionRequest {
    private Long subject_id;
    private String subject_name;
    private List<AssignmentGradeRequest> assigment_grades;
    private Float avg_score;
}
