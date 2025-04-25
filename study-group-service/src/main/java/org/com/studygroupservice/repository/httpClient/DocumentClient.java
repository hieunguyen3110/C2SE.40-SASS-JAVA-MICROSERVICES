package org.com.studygroupservice.repository.httpClient;

import org.com.studygroupservice.dto.response.ApiResponse;
import org.com.studygroupservice.dto.response.SubjectDto;
import org.springframework.web.service.annotation.GetExchange;

import java.util.List;

public interface DocumentClient {
    @GetExchange("/subject/getAllSubject")
    ApiResponse<List<SubjectDto>> getAllSubject();
}
