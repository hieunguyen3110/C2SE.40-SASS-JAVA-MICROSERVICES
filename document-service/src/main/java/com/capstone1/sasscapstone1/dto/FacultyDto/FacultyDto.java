package com.capstone1.sasscapstone1.dto.FacultyDto;

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
