package org.com.batchservice.writter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.com.batchservice.dto.response.MessageAnalyze;
import org.com.batchservice.handler.SendNotificationProducer;
import org.com.batchservice.repository.IdentityClient;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;
@Component
@RequiredArgsConstructor
@Slf4j
public class StudentLearnTrendAnalyzeWriterCustom implements ItemWriter<MessageAnalyze> {
    private final SendNotificationProducer sendNotificationProducer;
    private final IdentityClient identityClient;
    @Override
    public void write(Chunk<? extends MessageAnalyze> chunk) throws Exception {
        try{
            for(MessageAnalyze data : chunk){
                //save into database
                sendNotificationProducer.sendNotificationToAccount(data.getAccountId(),data.getMessage());
                String message= identityClient.updateAnalyzeMessage(data).getData();
                log.info(message);
            }
        }catch (Exception e){
            throw new RuntimeException("Failed to run job with exception: "+ e.getMessage());
        }
    }
}
