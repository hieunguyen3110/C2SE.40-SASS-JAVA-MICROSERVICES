package org.com.identityservice.repository.httpClient;

import org.com.identityservice.dto.response.AnalyzeData;
import org.com.identityservice.dto.response.ApiResponse;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;

public interface ELearningClient {
    @GetExchange("/analyze-data")
    ApiResponse<AnalyzeData> getAnalyzeDataByAccountId(@RequestParam("accountId") Long accountId);
}
