package org.com.identityservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ImprovementResponse {
    private String expected_outcome;
    private String focus_area;
    private String specific_action;
}
