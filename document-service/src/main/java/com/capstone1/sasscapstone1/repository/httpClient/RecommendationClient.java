package com.capstone1.sasscapstone1.repository.httpClient;

import com.capstone1.sasscapstone1.dto.response.ApiResponse;
import com.capstone1.sasscapstone1.enums.ErrorCode;
import com.capstone1.sasscapstone1.exception.ApiException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;

import java.util.List;

public interface RecommendationClient {
    @GetExchange("/document/get-by-user")
    @CircuitBreaker(name="recommendation-service",fallbackMethod = "fallbackRecommendation")
    @Retry(name = "recommendation-service")
    ApiResponse<List<String>> getDocumentByModel(@RequestParam("account_id") Long accountId);

    default ApiResponse<String> fallbackRecommendation(Long accountId, RuntimeException runtimeException) {
        throw new ApiException(ErrorCode.SERVICE_UNAVAILABLE.getStatusCode().value(), "Oops! Something went wrong, please try recommendation system again later!");
    }
}
