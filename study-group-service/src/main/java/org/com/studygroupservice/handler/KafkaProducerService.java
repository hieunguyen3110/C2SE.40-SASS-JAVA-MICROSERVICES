package org.com.studygroupservice.handler;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.com.studygroupservice.dto.response.MessageEventDto;
import org.com.studygroupservice.dto.response.StudyGroupEventDto;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaProducerService {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public void sendStudyGroupEvent(StudyGroupEventDto event) {
        try {
            String message = objectMapper.writeValueAsString(event);
            kafkaTemplate.send("study-group-ws-topic", event.getUserId().toString(), message);
            log.info("Sent study group event to Kafka: {}", message);
        } catch (JsonProcessingException e) {
            log.error("Error converting event to JSON: {}", e.getMessage());
        }
    }

    public void sendMessageEvent(MessageEventDto event) {
        try {
            String message = objectMapper.writeValueAsString(event);
            kafkaTemplate.send("chat-ws-topic", event.getGroupId().toString(), message);
            log.info("Sent message event to Kafka: {}", message);
        } catch (JsonProcessingException e) {
            log.error("Error converting event to JSON: {}", e.getMessage());
        }
    }
}
