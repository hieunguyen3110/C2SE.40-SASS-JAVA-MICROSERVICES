package org.com.elearningservice.controller;

import lombok.RequiredArgsConstructor;
import org.com.elearningservice.dto.request.EnableAnalyzeRequest;
import org.com.elearningservice.dto.response.AccountDto;
import org.com.elearningservice.dto.response.ApiResponse;
import org.com.elearningservice.dto.response.CoursePeriodDto;
import org.com.elearningservice.enums.ErrorCode;
import org.com.elearningservice.exception.ApiException;
import org.com.elearningservice.helper.CreateApiResponse;
import org.com.elearningservice.service.CoursePeriodService;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/course-period")
@RequiredArgsConstructor
public class CoursePeriodController {
    private final CoursePeriodService coursePeriodService;
    @GetMapping("/get-course-period")
    public ApiResponse<CoursePeriodDto> checkExistCoursePeriod(@RequestParam("accountId") Long accountId) throws Exception {
        return CreateApiResponse.createResponse(coursePeriodService.checkExistCoursePeriod(accountId),false);
    }
    @PostMapping("/save-course-period")
    public ApiResponse<String> saveCoursePeriod(@RequestBody EnableAnalyzeRequest request) throws Exception {
        Authentication authentication= SecurityContextHolder.getContext().getAuthentication();
        if(!(authentication instanceof AnonymousAuthenticationToken)){
            AccountDto accountDto= (AccountDto) authentication.getPrincipal();
            return CreateApiResponse.createResponse(coursePeriodService.saveCoursePeriod(request,accountDto),false);
        }else{
            throw new ApiException(ErrorCode.FORBIDDEN.getStatusCode().value(),"Session login expires");
        }
    }
}
