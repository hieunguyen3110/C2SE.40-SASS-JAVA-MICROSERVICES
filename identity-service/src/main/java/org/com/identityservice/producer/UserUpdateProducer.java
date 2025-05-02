package org.com.identityservice.producer;

import lombok.RequiredArgsConstructor;
import org.com.identityservice.dto.response.AccountDto;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserUpdateProducer {
    private final KafkaTemplate<String, String> kafkaTemplate;

    public void sendEventUpdateUser(String token, String accountJson) throws Exception {
        try{
            kafkaTemplate.send("user-update-topic",token,accountJson);
        }catch (Exception e){
            throw new Exception(e.getMessage());
        }
    }

}
