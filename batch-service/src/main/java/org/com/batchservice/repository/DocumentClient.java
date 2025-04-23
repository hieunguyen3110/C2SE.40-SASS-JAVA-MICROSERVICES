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
    @GetExchange("/cronjob/documents/get-by-day")
    ApiResponse<List<DocumentDto>> getDocumentsByDay();
    @GetExchange("/cronjob/collect-data")
    ApiResponse<MultipleNewData> collectData();
    @PostExchange("/cronjob/documents/update-file")
    ApiResponse<String> updateFileStatus(@RequestBody List<Long> docIds);
    @GetExchange("/cronjob/documents/new-upload")
    ApiResponse<List<DocumentDto>> getAllNewDocuments();
    @PostExchange("/cronjob/document/update-status-gen-question")
    ApiResponse<String> updateGenStatus(@RequestBody List<Long> docIds);
}
