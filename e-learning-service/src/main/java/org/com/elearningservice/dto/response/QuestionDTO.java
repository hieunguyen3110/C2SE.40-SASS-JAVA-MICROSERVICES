package org.com.elearningservice.dto.response;

import lombok.*;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class QuestionDTO {
    private String question;
    private String correctAnswer;
    private Map<String, Object> options;
}
