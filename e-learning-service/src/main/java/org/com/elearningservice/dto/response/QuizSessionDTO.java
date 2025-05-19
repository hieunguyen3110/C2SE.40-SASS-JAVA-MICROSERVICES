package org.com.elearningservice.dto.response;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuizSessionDTO {
    private Long accountId;
    private Long subjectId;
    private List<QuestionDTO> questions;
    private List<String> userAnswers;
    private boolean isAssignment;
    private Long docId;
}
