package org.com.elearningservice.dto.response;

import lombok.*;


@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GradeQuestionResponse {
    private QuestionDTO question;
    private String userAnswer;
}
