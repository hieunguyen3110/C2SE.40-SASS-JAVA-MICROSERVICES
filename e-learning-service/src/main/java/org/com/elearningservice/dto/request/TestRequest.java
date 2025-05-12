package org.com.elearningservice.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TestRequest {
    private Long subjectId;
    private Integer numberOfQuestions;
    private Integer duration;
}
