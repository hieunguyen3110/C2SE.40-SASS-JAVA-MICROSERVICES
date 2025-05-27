package org.com.elearningservice.dto.request;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AssignmentGradeRequest {
    private Long doc_id;
    private Float grade;
}
