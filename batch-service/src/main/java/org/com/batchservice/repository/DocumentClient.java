package org.com.batchservice.repository;

import org.com.batchservice.dto.response.*;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.PostExchange;

import java.util.List;

public interface DocumentClient {
    @GetExchange("/cronjob/ratings/not-checked")
    ApiResponse<List<RatingDto>> getAllRatingByDayAndIsCheckFalse();
    @PostExchange("/cronjob/ratings/update-checked")
    ApiResponse<String> updateRatingChecked(@RequestBody List<Long> ratingIds);
    @GetExchange("/cronjob/documents/")
    ApiResponse<List<DocumentDto>> getDocumentsByDay();
}
