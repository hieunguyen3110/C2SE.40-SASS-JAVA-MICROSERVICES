package com.capstone1.sasscapstone1.dto.CoursePeriodDto;

import lombok.*;

import java.util.Map;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CoursePeriodDto {
    private Long coursePeriodId;
    private Map<Long, String> subjects;
}
