package org.com.elearningservice.dto.response;

import lombok.Data;

import java.util.List;

@Data
public class QuestionDto {
    private Long id;
    private String content;
    private List<String> options;
    private String correctAnswer;
}
