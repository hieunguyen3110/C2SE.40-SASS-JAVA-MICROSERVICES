package org.com.batchservice.reader;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.com.batchservice.dto.response.DocumentDto;
import org.com.batchservice.repository.DocumentClient;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class CheckDocAndTrainDocReaderCustom implements ItemReader<DocumentDto> {
    private final DocumentClient documentClient;
    private List<DocumentDto> documentDtoList;
    private int currentIndex = 0;
    @Override
    public DocumentDto read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
        if (documentDtoList == null) {
            documentDtoList = documentClient.getDocumentsByDay().getData();
        }
        if (currentIndex < documentDtoList.size()) {
            return documentDtoList.get(currentIndex++);
        } else {
            return null;
        }
    }
}
