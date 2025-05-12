package org.com.elearningservice.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateAnswerRequest {
    private String userAnswer;
    private Boolean isAssignment;
    private int questionIndex;
}
