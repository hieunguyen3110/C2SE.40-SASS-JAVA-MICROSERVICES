package org.com.elearningservice.repository.http;

import org.com.elearningservice.dto.response.ApiResponse;
import org.com.elearningservice.dto.response.SubjectDTO;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

import java.util.List;

public interface DocumentClient {
    @GetExchange(url="/subject/getAllSubject")
    ApiResponse<List<SubjectDTO>> getAllSubject();
}
