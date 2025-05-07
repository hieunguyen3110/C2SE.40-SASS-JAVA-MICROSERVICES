package org.com.elearningservice.service;

import org.com.elearningservice.dto.request.EnableAnalyzeRequest;
import org.com.elearningservice.dto.response.AccountDto;
import org.com.elearningservice.dto.response.CoursePeriodDto;

public interface CoursePeriodService {
    CoursePeriodDto checkExistCoursePeriod(Long accountId) throws Exception;
    String saveCoursePeriod(EnableAnalyzeRequest request, AccountDto accountDto) throws Exception;
}
