package com.capstone1.sasscapstone1.controller.CronjobController;

import com.capstone1.sasscapstone1.dto.DocumentDto.DocumentDto;
import com.capstone1.sasscapstone1.dto.RatingDto.RatingDto;
import com.capstone1.sasscapstone1.dto.response.ApiResponse;
import com.capstone1.sasscapstone1.dto.response.DocumentData;
import com.capstone1.sasscapstone1.dto.response.MultipleNewData;
import com.capstone1.sasscapstone1.service.DocumentService.DocumentService;
import com.capstone1.sasscapstone1.service.RatingService.RatingService;
import com.capstone1.sasscapstone1.util.CreateApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/cronjob")
@RequiredArgsConstructor
public class CronjobController {
    private final RatingService ratingService;
    private final DocumentService documentService;
    @GetMapping("/ratings/not-checked")
    public ApiResponse<List<RatingDto>> getRatingNotCheckedByDay() throws Exception {
        return CreateApiResponse.createResponse(ratingService.getAllRatingByDayCheckedIsFalse(),false);
    }
    @PostMapping("/ratings/update-checked")
    public ApiResponse<String> updateRating(@RequestBody List<Long> ratingIds) throws Exception {
        return CreateApiResponse.createResponse(ratingService.saveRatingIsChecked(ratingIds),false);
    }
    @GetMapping("/ratings/collect-data")
    public ApiResponse<MultipleNewData> getDataNew(){
        return null;
    }
    @GetMapping("/documents/get-by-day")
    public ApiResponse<List<DocumentDto>> getALlDocumentByDay() throws Exception {
        return CreateApiResponse.createResponse(documentService.getAllDocumentByDay(),false);
    }
}
