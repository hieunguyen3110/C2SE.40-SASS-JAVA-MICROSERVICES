package org.com.elearningservice.dto.request;

import lombok.Data;

@Data
public class CreateTestRequest {
    private Long assessmentId;
    private String subject;
    private Long numberOfQuestions;
    private Long testDuration;
}
