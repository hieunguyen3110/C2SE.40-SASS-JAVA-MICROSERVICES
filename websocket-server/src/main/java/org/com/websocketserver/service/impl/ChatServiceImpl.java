package org.com.websocketserver.service.impl;

import lombok.RequiredArgsConstructor;
import org.com.websocketserver.dto.request.MessageRequest;
import org.com.websocketserver.handler.KafkaProducerHandler;
import org.com.websocketserver.service.ChatService;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {
    private final KafkaProducerHandler kafkaProducerHandler;
    private final SimpMessagingTemplate simpMessagingTemplate;
    @Override
    public void sendMessage(MessageRequest request, String senderId) throws Exception {
        try{
            kafkaProducerHandler.sendMessageToStudyGroup(request, senderId);
            simpMessagingTemplate.convertAndSendToUser(request.getGroupId().toString(),"/queue/messages",request);
        }catch (Exception e){
            throw new Exception(e);
        }
    }
}
