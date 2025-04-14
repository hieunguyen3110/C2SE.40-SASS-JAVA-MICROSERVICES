package org.com.batchservice.repository;

import org.com.batchservice.dto.response.ApiResponse;
import org.com.batchservice.dto.response.MultipleNewData;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.PostExchange;

public interface RecommendationClient {
    @PostExchange("/document/collect-data")
    ApiResponse<String> loadData(@RequestBody MultipleNewData data);
}
