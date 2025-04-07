package org.com.batchservice.reader;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.com.batchservice.repository.DocumentClient;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
@Setter
@Getter
public class CollectDataReaderCustom implements ItemReader<Object> {
    private final DocumentClient documentClient;
    @Override
    public Object read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
        return null;
    }
}
