package org.com.batchservice.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CoursePeriodRequest {
    private Long subject_id;
    private String subject_name;
}
