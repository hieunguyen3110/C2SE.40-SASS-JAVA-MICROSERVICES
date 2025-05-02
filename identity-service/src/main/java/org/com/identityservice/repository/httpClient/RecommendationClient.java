package org.com.identityservice.repository.httpClient;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.com.identityservice.dto.request.AnalyzeRequest;
import org.com.identityservice.dto.response.ApiResponse;
import org.com.identityservice.enums.ErrorCode;
import org.com.identityservice.exception.ApiException;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.PostExchange;

import java.util.List;

public interface RecommendationClient {
    @PostExchange("/get-solution")
    @CircuitBreaker(name="recommendation-service",fallbackMethod = "fallbackRecommendation")
    @Retry(name = "recommendation-service")
    ApiResponse<String> getSolutionByAI(@RequestBody AnalyzeRequest analyzeRequest);

    default ApiResponse<String> fallbackRecommendation(Long accountId, RuntimeException runtimeException) {
        throw new ApiException(ErrorCode.BAD_REQUEST.getStatusCode().value(), "Oops! Something went wrong, please try recommendation system again later!");
    }
}
