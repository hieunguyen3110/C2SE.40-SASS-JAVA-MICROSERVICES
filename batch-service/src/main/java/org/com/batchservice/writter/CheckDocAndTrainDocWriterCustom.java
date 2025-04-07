package org.com.batchservice.writter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class CheckDocAndTrainDocWriterCustom implements ItemWriter<Object> {
    @Override
    public void write(Chunk<?> chunk) throws Exception {

    }
}
