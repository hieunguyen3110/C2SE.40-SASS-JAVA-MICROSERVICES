package org.com.studygroupservice.repository.httpClient;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.com.studygroupservice.dto.response.ApiResponse;
import org.com.studygroupservice.dto.response.SubjectDto;
import org.com.studygroupservice.enums.ErrorCode;
import org.com.studygroupservice.exception.ApiException;
import org.springframework.web.service.annotation.GetExchange;

import java.util.List;

public interface DocumentClient {
    @GetExchange("/subject/getAllSubject")
    @CircuitBreaker(name = "document-service", fallbackMethod = "fallbackGetSubjects")
    @Retry(name = "document-service")
    ApiResponse<List<SubjectDto>> getAllSubject();

    default ApiResponse<List<SubjectDto>> fallbackGetSubjects(Throwable throwable){
        throw new ApiException(ErrorCode.SERVICE_UNAVAILABLE.getStatusCode().value(),"Oops! Something went wrong, please try document service again later!");
    }
}
