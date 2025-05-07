package com.capstone1.sasscapstone1.repository.httpClient;

import com.capstone1.sasscapstone1.dto.CoursePeriodDto.CoursePeriodDto;
import com.capstone1.sasscapstone1.dto.response.ApiResponse;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;

public interface ELearningClient {
    @GetExchange("/course-period/get-course-period")
    ApiResponse<CoursePeriodDto> checkExistCoursePeriod(@RequestParam("accountId") Long accountId);
}
