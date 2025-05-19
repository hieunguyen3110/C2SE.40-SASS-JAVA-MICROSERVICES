package org.com.batchservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.com.batchservice.dto.request.AssignmentCompletionRequest;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LearningAnalyzeResponse {
    private GeneralAssessment general_assessment;
    private List<ImprovementResponse> improvement_suggestions;
    private ProgressTracking progress_tracking;
    private WeeklyStudyPlan weekly_study_plan;
    private List<AssignmentCompletionRequest> subject_weakens;
    private List<DocumentDto> document_recommend;
    private List<DocumentDto> document_read_again;
}
