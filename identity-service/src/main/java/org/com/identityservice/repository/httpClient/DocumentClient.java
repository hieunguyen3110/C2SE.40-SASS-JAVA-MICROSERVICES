package org.com.identityservice.repository.httpClient;

import org.com.identityservice.dto.response.AccountStatisticsDto;
import org.com.identityservice.dto.response.ApiResponse;
import org.com.identityservice.dto.response.FacultyDto;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;

public interface DocumentClient {
    @GetExchange(url="/user/count-stats")
    ApiResponse<AccountStatisticsDto> countAccountStaticsDto(@RequestParam("accountId") long accountId);
    @GetExchange(url="/faculty/{facultyId}")
    ApiResponse<FacultyDto> getFacultyById(@PathVariable("facultyId") long facultyId);
}
