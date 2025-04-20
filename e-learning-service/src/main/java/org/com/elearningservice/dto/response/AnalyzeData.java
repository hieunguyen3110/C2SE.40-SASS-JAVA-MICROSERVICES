package org.com.elearningservice.dto.response;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AnalyzeData {
    private Long accountId;
    private Float examScore;
    private Float assignmentScore;
    private Integer onlineCourseComplete;
    private Integer onlineTestComplete;
}
