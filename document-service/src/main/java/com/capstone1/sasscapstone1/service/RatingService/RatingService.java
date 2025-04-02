package com.capstone1.sasscapstone1.service.RatingService;

import com.capstone1.sasscapstone1.dto.AccountDto.AccountDto;
import com.capstone1.sasscapstone1.request.RatingRequest;

public interface RatingService {
    String rateDocumentByUser(AccountDto accountDto, RatingRequest request) throws Exception;
}
