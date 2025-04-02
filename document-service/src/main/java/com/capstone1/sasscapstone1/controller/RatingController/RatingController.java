package com.capstone1.sasscapstone1.controller.RatingController;

import com.capstone1.sasscapstone1.dto.AccountDto.AccountDto;
import com.capstone1.sasscapstone1.dto.response.ApiResponse;
import com.capstone1.sasscapstone1.enums.ErrorCode;
import com.capstone1.sasscapstone1.exception.ApiException;
import com.capstone1.sasscapstone1.request.RatingRequest;
import com.capstone1.sasscapstone1.service.RatingService.RatingService;
import com.capstone1.sasscapstone1.util.CreateApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ratings")
@RequiredArgsConstructor
public class RatingController {
    private final RatingService ratingService;

    @PostMapping("/rate")
    public ApiResponse<String> handleDocumentRatingByUser(@RequestBody RatingRequest request) throws Exception {
        Authentication authentication= SecurityContextHolder.getContext().getAuthentication();
        if(!(authentication instanceof AnonymousAuthenticationToken)){
            AccountDto accountDto= (AccountDto) authentication.getPrincipal();
            return CreateApiResponse.createResponse(ratingService.rateDocumentByUser(accountDto,request), false);
        }else{
            throw new ApiException(ErrorCode.FORBIDDEN.getStatusCode().value(), "Account isn't permission!");
        }
    }
}
