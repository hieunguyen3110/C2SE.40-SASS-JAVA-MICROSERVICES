package org.com.identityservice.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.com.identityservice.dto.response.SubjectDto;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class EnableAnalyzeRequest {
    private List<SubjectDto> subjects;
}
