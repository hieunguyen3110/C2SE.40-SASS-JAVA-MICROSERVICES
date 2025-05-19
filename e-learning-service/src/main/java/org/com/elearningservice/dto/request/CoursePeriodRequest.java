package org.com.elearningservice.dto.request;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CoursePeriodRequest {
    private Long subject_id;
    private String subject_name;
}
