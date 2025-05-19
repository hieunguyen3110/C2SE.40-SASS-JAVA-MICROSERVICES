package org.com.batchservice.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class AssignmentGradeRequest {
    private Long docId;
    private Float grade;
}
