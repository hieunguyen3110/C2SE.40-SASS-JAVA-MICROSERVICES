package com.capstone1.sasscapstone1.service.RatingService;

import com.capstone1.sasscapstone1.dto.AccountDto.AccountDto;
import com.capstone1.sasscapstone1.dto.RatingDto.RatingDto;
import com.capstone1.sasscapstone1.dto.response.UserInteractionData;
import com.capstone1.sasscapstone1.request.RatingRequest;

import java.util.List;

public interface RatingService {
    String rateDocumentByUser(AccountDto accountDto, RatingRequest request) throws Exception;
    List<RatingDto> getAllRatingByDayCheckedIsFalse() throws Exception;
    List<UserInteractionData> getAllRatingByDayCheckedIsTrue() throws Exception;
    String saveRatingIsChecked(List<Long> ratingIds) throws Exception;
}
