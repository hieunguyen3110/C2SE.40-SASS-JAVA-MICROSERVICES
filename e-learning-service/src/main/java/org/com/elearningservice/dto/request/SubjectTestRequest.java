package org.com.elearningservice.dto.request;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubjectTestRequest {
    private Long subject_id;
    private String subject_name;
    private Float avg_score;
}
