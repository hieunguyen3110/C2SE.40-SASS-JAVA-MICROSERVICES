package com.capstone1.sasscapstone1.repository.httpClient;

import com.capstone1.sasscapstone1.dto.ChatbotDTO.ChatbotResponse;
import com.capstone1.sasscapstone1.dto.CheckFileResponse.CheckFileResponse;
import com.capstone1.sasscapstone1.dto.response.ApiResponse;
import com.capstone1.sasscapstone1.request.SendMessageRequest;
import com.capstone1.sasscapstone1.request.TrainDocumentRequest;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.PostExchange;

import java.util.Map;

public interface ChatbotClient {
    @PostExchange("/search")
    ApiResponse<ChatbotResponse> sendQuery(@RequestBody SendMessageRequest request);
    @PostExchange("/check-file")
    ApiResponse<CheckFileResponse> checkFile(@RequestBody Map<String, String> request);
    @PostExchange("/upload-file")
    ApiResponse<String> trainFile(@RequestBody TrainDocumentRequest request);
}
