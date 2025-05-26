package com.capstone1.sasscapstone1.service.ChatbotService;

import com.capstone1.sasscapstone1.dto.ChatbotDTO.ChatbotDto;
import com.capstone1.sasscapstone1.dto.ChatbotDTO.ChatbotResponse;
import com.capstone1.sasscapstone1.dto.response.ApiResponse;
import com.capstone1.sasscapstone1.request.SendMessageRequest;
import org.springframework.stereotype.Service;

@Service
public interface ChatbotService {
    ApiResponse<ChatbotDto> sendMessage(SendMessageRequest request) throws Exception;
}
