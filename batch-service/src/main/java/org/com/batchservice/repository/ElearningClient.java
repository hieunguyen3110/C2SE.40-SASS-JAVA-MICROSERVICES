package org.com.batchservice.repository;

import org.com.batchservice.dto.response.AnalyzeData;
import org.com.batchservice.dto.response.ApiResponse;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.PostExchange;

import java.util.List;

public interface ElearningClient {
    @PostExchange("/cronjob/e-learning/account-analyze-data")
    ApiResponse<List<AnalyzeData>> getDataAnalyzeByAccountIds(@RequestBody List<Long> accountIds);
}
