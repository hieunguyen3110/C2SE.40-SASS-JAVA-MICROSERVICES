package org.com.identityservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class FacultyDto {
    private Long facultyId;
    private String facultyName;
}
