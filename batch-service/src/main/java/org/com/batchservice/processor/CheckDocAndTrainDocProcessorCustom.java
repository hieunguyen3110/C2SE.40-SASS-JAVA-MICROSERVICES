package org.com.batchservice.processor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.com.batchservice.repository.ChatbotClient;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class CheckDocAndTrainDocProcessorCustom implements ItemProcessor<Object, Object> {
    private final ChatbotClient chatbotClient;
    @Override
    public Object process(Object item) throws Exception {
        return null;
    }
}
