package com.capstone1.sasscapstone1.controller.ChatbotController;

import com.capstone1.sasscapstone1.dto.response.ApiResponse;
import com.capstone1.sasscapstone1.enums.ErrorCode;
import com.capstone1.sasscapstone1.exception.ApiException;
import com.capstone1.sasscapstone1.request.SendMessageRequest;
import com.capstone1.sasscapstone1.service.ChatbotService.ChatbotService;
import com.capstone1.sasscapstone1.util.CreateApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/chat-bot")
@RequiredArgsConstructor
public class ChatbotController {
    private final ChatbotService chatbotService;

    @PostMapping("/send-message")
    public ApiResponse<?> sendMessage(@RequestBody SendMessageRequest request){
        try{
            Authentication authentication= SecurityContextHolder.getContext().getAuthentication();
            if(!(authentication instanceof AnonymousAuthenticationToken)){
                return chatbotService.sendMessage(request);
            }else{
                return CreateApiResponse.createResponse("You must login",false);
            }
        }catch (Exception e){
            throw new ApiException(ErrorCode.INTERNAL_SERVER_ERROR.getStatusCode().value(), e.getMessage());
        }
    }
}
