package org.com.batchservice.writter;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
@Setter
@Getter
public class CollectDataWriterCustom implements ItemWriter<Object> {
    @Override
    public void write(Chunk<?> chunk) throws Exception {

    }
}
