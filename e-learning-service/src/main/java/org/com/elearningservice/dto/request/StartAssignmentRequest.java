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
    private Integer numberOfQuestion;
    private Integer duration;
}
