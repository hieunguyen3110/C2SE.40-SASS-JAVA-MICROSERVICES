package org.com.elearningservice.repository.http;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.com.elearningservice.dto.response.ApiResponse;
import org.com.elearningservice.dto.response.SubjectDTO;
import org.com.elearningservice.enums.ErrorCode;
import org.com.elearningservice.exception.ApiException;
import org.springframework.web.service.annotation.GetExchange;

import java.util.List;

public interface DocumentClient {
    @CircuitBreaker(name="document-service", fallbackMethod = "fallbackGetSubjects")
    @Retry(name = "document-service")
    @GetExchange(url="/subject/getAllSubject")
    ApiResponse<List<SubjectDTO>> getAllSubject();

    default ApiResponse<List<SubjectDTO>> fallbackGetSubjects(Throwable throwable){
        throw new ApiException(ErrorCode.SERVICE_UNAVAILABLE.getStatusCode().value(),"Oops! Something went wrong, please try document service again later!");
    }
}
