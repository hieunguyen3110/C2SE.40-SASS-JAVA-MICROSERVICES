package org.com.batchservice.repository;

import org.com.batchservice.dto.request.AnalyzeRequest;
import org.com.batchservice.dto.response.ApiResponse;
import org.com.batchservice.dto.response.MultipleNewData;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.PostExchange;

import java.util.List;

public interface RecommendationClient {
    @PostExchange("/document/collect-data")
    ApiResponse<String> loadData(@RequestBody MultipleNewData data);

    @PostExchange("/get-solution")
    ApiResponse<String> predictStudentLearningTrending(@RequestBody AnalyzeRequest request);
}
