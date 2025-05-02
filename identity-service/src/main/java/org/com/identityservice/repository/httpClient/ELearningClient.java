package org.com.identityservice.repository.httpClient;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.com.identityservice.dto.response.AnalyzeData;
import org.com.identityservice.dto.response.ApiResponse;
import org.com.identityservice.enums.ErrorCode;
import org.com.identityservice.exception.ApiException;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;

public interface ELearningClient {
    @GetExchange("/analyze-data")
    @CircuitBreaker(name="e-learning-service",fallbackMethod = "fallbackELearning")
    @Retry(name = "e-learning-service")
    ApiResponse<AnalyzeData> getAnalyzeDataByAccountId(@RequestParam("accountId") Long accountId);

    default ApiResponse<AnalyzeData> fallbackELearning(Long accountId, RuntimeException e){
        throw new ApiException(ErrorCode.BAD_REQUEST.getStatusCode().value(), "Oops! Something went wrong, please try e-learning again later!");
    }
}
