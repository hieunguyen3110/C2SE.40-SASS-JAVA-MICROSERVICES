package org.com.batchservice.processor;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.com.batchservice.dto.response.RatingDto;
import org.com.batchservice.helpers.SensitiveWordChecker;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
@Component
@RequiredArgsConstructor
@Slf4j
public class CheckingRatingContentProcessorCustom implements ItemProcessor<RatingDto, RatingDto> {
    private final SensitiveWordChecker sensitiveWordChecker;
    @Override
    public RatingDto process(@NonNull RatingDto item) throws Exception {
        try{
            if(!sensitiveWordChecker.containsSensitiveWord(item.getContent())){
                return item;
            }else{
                return null;
            }
        }catch (Exception e){
            log.error("Exception: "+ e.getMessage());
            throw new Exception(e.getMessage());
        }
    }
}
