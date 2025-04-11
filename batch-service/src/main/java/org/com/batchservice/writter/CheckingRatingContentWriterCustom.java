package org.com.batchservice.writter;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.com.batchservice.dto.response.RatingDto;
import org.com.batchservice.repository.DocumentClient;
import org.jetbrains.annotations.NotNull;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

import java.util.List;
@Component
@RequiredArgsConstructor
@Slf4j
public class CheckingRatingContentWriterCustom implements ItemWriter<RatingDto> {
    private final DocumentClient documentClient;
    @Override
    @Transactional
    public void write(Chunk<? extends RatingDto> chunk) {
        try{
            List<Long> ratingIds = chunk.getItems().stream()
                    .map(RatingDto::getRateId)
                    .toList();
            if (!ratingIds.isEmpty()) {
                String response = documentClient.updateRatingChecked(ratingIds).getData();
                log.info("Message: "+ response);
                log.info("Cập nhật thành công các rating có ID: " + ratingIds);
            }
        }catch (Exception e){
            throw new RuntimeException("Failed to run job with exception: "+ e.getMessage());
        }
    }
}
