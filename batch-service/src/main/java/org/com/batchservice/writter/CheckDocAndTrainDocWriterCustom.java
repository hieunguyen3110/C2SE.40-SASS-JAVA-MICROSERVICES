package org.com.batchservice.writter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.com.batchservice.dto.response.DocumentDto;
import org.com.batchservice.repository.DocumentClient;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class CheckDocAndTrainDocWriterCustom implements ItemWriter<DocumentDto> {
    private final DocumentClient documentClient;
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void write(Chunk<? extends DocumentDto> chunk) throws Exception {
        try{
            List<Long> docIds= chunk.getItems().stream()
                    .map(DocumentDto::getDocId)
                    .toList();
            if(!docIds.isEmpty()){
                String response= documentClient.updateFileStatus(docIds).getData();
                log.info("Message: "+ response);
                log.info("Cập nhật thành công các document có ID: " + docIds);
            }
        }catch (Exception e){
            throw new Exception(e.getMessage());
        }
    }
}
