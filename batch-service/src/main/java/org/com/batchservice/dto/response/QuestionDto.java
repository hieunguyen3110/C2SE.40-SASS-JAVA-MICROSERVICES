package org.com.batchservice.dto.response;

import lombok.*;

import java.util.Map;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionDto {
    private String correctAnswer;
    private Map<String,Object> options;
    private String question;
}
