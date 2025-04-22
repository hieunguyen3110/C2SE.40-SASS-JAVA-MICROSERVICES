package org.com.websocketserver.service.impl;

import lombok.RequiredArgsConstructor;
import org.com.websocketserver.dto.request.MessageRequest;
import org.com.websocketserver.handler.KafkaProducerHandler;
import org.com.websocketserver.service.ChatService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {
    private final KafkaProducerHandler kafkaProducerHandler;
    @Override
    public void sendMessage(MessageRequest request, String senderId) throws Exception {
        try{
            kafkaProducerHandler.sendMessageToStudyGroup(request, senderId);
        }catch (Exception e){
            throw new Exception(e);
        }
    }
}
