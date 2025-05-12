package org.com.elearningservice.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SubmitRequest {
    private Long subjectId;
    private Boolean isAssignment;
    private List<String> userAnswers;
}
