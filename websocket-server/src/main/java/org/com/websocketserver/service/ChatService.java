package org.com.websocketserver.service;

import org.com.websocketserver.dto.request.MessageRequest;

public interface ChatService {
    void sendMessage(MessageRequest request, String senderId) throws Exception;
}
