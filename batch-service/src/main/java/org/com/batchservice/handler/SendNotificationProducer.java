package org.com.batchservice.handler;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.com.batchservice.dto.request.NotificationEventRequest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class SendNotificationProducer {
    private final KafkaTemplate<String,String> kafkaTemplate;

    public void sendNotificationToAccount(List<Long> accountIds) throws Exception {
        try{
            String message= "Bạn cần học tập nghiêm túc và hoàn thành đầy đủ bài tập cũng như các bài quiz để nắm vững kiến thức.";
            ObjectMapper objectMapper= new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());
            objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            for(Long accountId : accountIds){
                NotificationEventRequest request= NotificationEventRequest.builder()
                        .accountId(accountId)
                        .message(message)
                        .type("LEARNING")
                        .build();
                String notificationJson = objectMapper.writeValueAsString(request);
                kafkaTemplate.send("e-learning-topic",accountId.toString() ,notificationJson);
            }
        }catch (Exception e){
            log.error("Error when send event to topic");
            throw new Exception("Error when send event to topic: "+ e);
        }
    }
    public void sendNotificationToAccount(Long accountId, String message) throws Exception {
        try{
            ObjectMapper objectMapper= new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());
            objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            NotificationEventRequest request= NotificationEventRequest.builder()
                    .accountId(accountId)
                    .message(message)
                    .type("LEARNING")
                    .build();
            String notificationJson = objectMapper.writeValueAsString(request);
            kafkaTemplate.send("e-learning-topic",accountId.toString() ,notificationJson);
        }catch (Exception e){
            log.error("Error when send event to topic");
            throw new Exception("Error when send event to topic: "+ e);
        }
    }
}
