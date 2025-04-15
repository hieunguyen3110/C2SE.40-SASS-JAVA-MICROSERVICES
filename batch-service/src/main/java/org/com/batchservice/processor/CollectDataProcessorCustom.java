package org.com.batchservice.processor;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.com.batchservice.dto.response.MultipleNewData;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
@Setter
@Getter
public class CollectDataProcessorCustom implements ItemProcessor<MultipleNewData,MultipleNewData> {
    @Override
    public MultipleNewData process(MultipleNewData item) throws Exception {
        return item;
    }
}
