package com.capstone1.sasscapstone1.controller.DocumentViewController;

import com.capstone1.sasscapstone1.dto.AccountDto.AccountDto;
import com.capstone1.sasscapstone1.dto.response.ApiResponse;
import com.capstone1.sasscapstone1.enums.ErrorCode;
import com.capstone1.sasscapstone1.exception.ApiException;
import com.capstone1.sasscapstone1.request.ViewTimeRequest;
import com.capstone1.sasscapstone1.service.DocumentViewService.DocumentViewService;
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
@RequiredArgsConstructor
@RequestMapping("/tracking")
public class DocumentViewController {
    private final DocumentViewService documentViewService;
    @PostMapping("/view-time-document")
    public ApiResponse<String> saveViewLog(@RequestBody ViewTimeRequest request) throws Exception {
        Authentication authentication= SecurityContextHolder.getContext().getAuthentication();
        if(!(authentication instanceof AnonymousAuthenticationToken)){
            AccountDto accountDto= (AccountDto) authentication.getPrincipal();
            documentViewService.saveViewLog(accountDto,request);
            return CreateApiResponse.createResponse("Save view log successful",false);
        }else{
            throw new ApiException(ErrorCode.FORBIDDEN.getStatusCode().value(),"Account not permission");
        }
    }
}
