package com.capstone1.sasscapstone1.controller.HistoryController;

import com.capstone1.sasscapstone1.dto.AccountDto.AccountDto;
import com.capstone1.sasscapstone1.dto.response.ApiResponse;
import com.capstone1.sasscapstone1.enums.ErrorCode;
import com.capstone1.sasscapstone1.exception.ApiException;
import com.capstone1.sasscapstone1.service.HistoryService.HistoryService;
import com.capstone1.sasscapstone1.util.CreateApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/history")
@RequiredArgsConstructor
public class HistoryController {
    private final HistoryService historyService;

    // Track document download
    @PostMapping("/track")
    public ApiResponse<String> trackDownload(@RequestParam Long docId) {
        Authentication authentication= SecurityContextHolder.getContext().getAuthentication();
        if(!(authentication instanceof AnonymousAuthenticationToken)){
            AccountDto accountDto= (AccountDto) authentication.getPrincipal();
            historyService.trackDownload(docId, accountDto.getAccountId());
            return CreateApiResponse.createResponse("Download tracked successfully",false);
        }else{
            throw new ApiException(ErrorCode.FORBIDDEN.getStatusCode().value(),"Account not permission");
        }
    }
}


