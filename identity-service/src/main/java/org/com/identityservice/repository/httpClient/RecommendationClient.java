package org.com.identityservice.repository.httpClient;

import org.com.identityservice.dto.request.AnalyzeRequest;
import org.com.identityservice.dto.response.ApiResponse;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.PostExchange;

import java.util.List;

public interface RecommendationClient {
    @PostExchange("/get-solution")
    ApiResponse<String> getSolutionByAI(@RequestBody AnalyzeRequest analyzeRequest);
}
