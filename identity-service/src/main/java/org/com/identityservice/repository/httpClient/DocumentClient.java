package org.com.identityservice.repository.httpClient;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.com.identityservice.dto.response.AccountStatisticsDto;
import org.com.identityservice.dto.response.ApiResponse;
import org.com.identityservice.dto.response.FacultyDto;
import org.com.identityservice.enums.ErrorCode;
import org.com.identityservice.exception.ApiException;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;

public interface DocumentClient {
    @GetExchange(url="/user/count-stats")
    @CircuitBreaker(name="document-service")
    @Retry(name = "document-service")
    ApiResponse<AccountStatisticsDto> countAccountStaticsDto(@RequestParam("accountId") long accountId);
    @CircuitBreaker(name="document-service")
    @Retry(name = "document-service")
    @GetExchange(url="/faculty/{facultyId}")
    ApiResponse<FacultyDto> getFacultyById(@PathVariable("facultyId") long facultyId);

    default ApiResponse<AccountStatisticsDto> fallbackAccountStaticDto(long accountId, Throwable throwable){
        throw new ApiException(ErrorCode.SERVICE_UNAVAILABLE.getStatusCode().value(),"Oops! Something went wrong, please try document service again later!");
    }
    default ApiResponse<FacultyDto> fallbackFacultyDto(long facultyId, Throwable throwable){
        throw new ApiException(ErrorCode.SERVICE_UNAVAILABLE.getStatusCode().value(),"Oops! Something went wrong, please try document service again later!");
    }
}
