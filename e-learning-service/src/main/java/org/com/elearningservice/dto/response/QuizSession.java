package org.com.elearningservice.dto.response;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class QuizSession {
    private List<QuestionDto> questions;
    private Map<Integer, String> answers;
    private Long quizId;
    private Long accountId;
    private Long assignmentId;
    private String subject;
    private Long testDuration;
    private Long startTime;
}
