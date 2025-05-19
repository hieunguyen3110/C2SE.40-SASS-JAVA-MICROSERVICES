package org.com.elearningservice.dto.response;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AssignmentSessionDto {
    private Long accountId;
    private Long subjectId;
    private Long docId;
    private List<QuestionDTO> questions;
    private List<String> userAnswers;
}
