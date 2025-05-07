package org.com.elearningservice.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.com.elearningservice.dto.response.SubjectDTO;

import java.util.List;
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class EnableAnalyzeRequest {
    private List<SubjectDTO> subjects;
}
