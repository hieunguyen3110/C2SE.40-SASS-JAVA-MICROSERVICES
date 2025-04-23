package org.com.websocketserver.controller;

import lombok.RequiredArgsConstructor;
import org.com.websocketserver.dto.request.MessageRequest;
import org.com.websocketserver.service.ChatService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class ChatController {
    private final ChatService chatService;
    @MessageMapping("/chat.private")
    public void sendMessage(@Payload MessageRequest messageRequest, Principal principal) throws Exception {
        String senderId= principal.getName();
        chatService.sendMessage(messageRequest, senderId);
    }

}
