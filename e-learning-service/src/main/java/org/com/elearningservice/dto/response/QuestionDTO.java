package org.com.elearningservice.dto.response;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class QuestionDTO {
    private String question;
    private String correctAnswer;
    private Map<String, String> options;
}
