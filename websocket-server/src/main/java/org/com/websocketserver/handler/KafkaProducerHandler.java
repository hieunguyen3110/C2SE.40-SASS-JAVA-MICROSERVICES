package org.com.websocketserver.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.com.websocketserver.dto.request.MessageRequest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaProducerHandler {
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    public void sendMessageToStudyGroup(MessageRequest request, String senderId) throws Exception {
        try{
            String json= objectMapper.writeValueAsString(request);
            kafkaTemplate.send("send-message-topic",senderId,json);
        }catch (Exception e){
            throw new Exception(e);
        }
    }

}
