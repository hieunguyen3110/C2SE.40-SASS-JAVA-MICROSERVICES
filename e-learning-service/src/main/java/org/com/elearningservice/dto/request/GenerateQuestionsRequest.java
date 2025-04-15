package org.com.elearningservice.dto.request;

import lombok.Data;

@Data
public class GenerateQuestionsRequest {
    private AssessmentRequest assessment;
    private String subjectName;
    private Long numberOfQuestions;
    private Long testDuration;
}
