package org.com.elearningservice.dto.response;

import lombok.Data;

import java.util.List;

@Data
public class QuizSessionDTO {
    private Long userId;
    private Long subjectId;
    private List<QuestionDTO> questions;
    private List<String> userAnswers;
    private boolean isAssignment;
}
