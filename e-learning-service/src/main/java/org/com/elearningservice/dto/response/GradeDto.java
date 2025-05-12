package org.com.elearningservice.dto.response;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GradeDto {
    private Long id;
    private Long subjectId;
    private Float score;
    private Integer totalQuestions;
    private String type;
    private List<GradeQuestionResponse> gradeQuestions;
}
