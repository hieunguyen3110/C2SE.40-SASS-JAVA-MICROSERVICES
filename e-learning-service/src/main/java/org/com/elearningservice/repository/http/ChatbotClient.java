package org.com.elearningservice.repository.http;

import org.com.elearningservice.dto.response.ApiResponse;
import org.com.elearningservice.dto.response.QuestionResponse;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;

import java.util.List;

public interface ChatbotClient {
    @GetExchange("/document/generate-question")
    ApiResponse<List<QuestionResponse>> generateQuestion(@RequestParam("docIds") List<Long> docIds);
}
