package com.capstone1.sasscapstone1.repository.httpClient;

import com.capstone1.sasscapstone1.dto.response.ApiResponse;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;

import java.util.List;

public interface RecommendationClient {
    @GetExchange("/document/get-by-user")
    ApiResponse<List<String>> getDocumentByModel(@RequestParam("account_id") Long accountId);
}
