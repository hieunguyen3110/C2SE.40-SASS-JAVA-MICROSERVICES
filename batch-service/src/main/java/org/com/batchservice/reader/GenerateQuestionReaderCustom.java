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
public class GenerateQuestionReaderCustom implements ItemReader<DocumentDto> {
    private final DocumentClient documentClient;
    private List<DocumentDto> documentDtos;
    private boolean readDone = false;
    private int currentIndex=0;
    @Override
    public DocumentDto read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
        if(readDone) return null;
        try{
            if(documentDtos==null){
                documentDtos= documentClient.getAllNewDocuments().getData();
                if (documentDtos == null || documentDtos.isEmpty()) {
                    readDone = true;
                    return null;
                }
            }
            if(currentIndex<documentDtos.size()){
                return documentDtos.get(currentIndex++);
            }else{
                readDone= true;
                return null;
            }
        }catch (Exception e){
            log.error("Error while reading data for new documents", e);
            throw new Exception("Failed to read data in GenerateQuestionReaderCustom", e);
        }
    }
}
