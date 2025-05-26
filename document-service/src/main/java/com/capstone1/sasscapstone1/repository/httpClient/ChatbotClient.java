package com.capstone1.sasscapstone1.repository.httpClient;

import com.capstone1.sasscapstone1.dto.ChatbotDTO.ChatbotResponse;
import com.capstone1.sasscapstone1.dto.response.ApiResponse;
import com.capstone1.sasscapstone1.enums.ErrorCode;
import com.capstone1.sasscapstone1.exception.ApiException;
import com.capstone1.sasscapstone1.request.SendMessageRequest;
import com.capstone1.sasscapstone1.request.TrainDocumentRequest;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.PostExchange;

public interface ChatbotClient {
    @PostExchange("/upload-file")
    @CircuitBreaker(name="chatbot-service", fallbackMethod = "fallbackChatbot")
    @Retry(name = "chatbot-service")
    ApiResponse<String> trainFile(@RequestBody TrainDocumentRequest request);

    @PostExchange("/search")
    ApiResponse<ChatbotResponse> sendMessage(@RequestBody SendMessageRequest request);

    default ApiResponse<String> fallbackChatbot(TrainDocumentRequest request,Throwable throwable) {
        throw new ApiException(ErrorCode.BAD_REQUEST.getStatusCode().value(), "Oops! Something went wrong, please try chatbot again later!");
    }
}
