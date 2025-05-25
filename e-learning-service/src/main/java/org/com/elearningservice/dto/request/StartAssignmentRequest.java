package org.com.elearningservice.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class StartAssignmentRequest {
    private Long docId;
    private Long subjectId;
    private Integer numberOfQuestions=5;
    private Integer duration=10;
}
