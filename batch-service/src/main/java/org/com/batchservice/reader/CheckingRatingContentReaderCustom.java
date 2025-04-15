package org.com.batchservice.reader;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.com.batchservice.dto.response.RatingDto;
import org.com.batchservice.repository.DocumentClient;
import org.springframework.batch.item.ItemReader;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
@Setter
@Getter
public class CheckingRatingContentReaderCustom implements ItemReader<RatingDto> {
    private final DocumentClient documentClient;
    private List<RatingDto> ratingList;
    private int currentIndex = 0;

    @Override
    public RatingDto read() throws Exception {
        if (ratingList == null) {
            ratingList = documentClient.getAllRatingByDayAndIsCheckFalse().getData();
        }
        if (currentIndex < ratingList.size()) {
            return ratingList.get(currentIndex++);
        } else {
            return null;
        }
    }
}
