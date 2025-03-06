package org.com.identityservice.producer;

import lombok.RequiredArgsConstructor;
import org.com.identityservice.dto.response.AccountDto;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserUpdateProducer {
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendEventUpdateUser(String token, AccountDto accountDto) throws Exception {
        try{
            kafkaTemplate.send("user-update-topic",token,accountDto);
        }catch (Exception e){
            throw new Exception(e.getMessage());
        }
    }

}
