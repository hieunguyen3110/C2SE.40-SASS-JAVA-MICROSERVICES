package org.com.batchservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class GeneralAssessment {
    private String overall_status;
    private String risk_assessment;
    private List<String> strengths;
    private List<String> weaknesses;
}
