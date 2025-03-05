package com.capstone1.sasscapstone1.repository.httpClient;

import com.capstone1.sasscapstone1.dto.AccountDto.AccountDto;
import com.capstone1.sasscapstone1.dto.response.ApiResponse;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;

public interface IdentityClient {
    @GetExchange(url="/account/{accountId}")
    ApiResponse<AccountDto> getAccountId(@PathVariable("accountId") long accountId);
    @GetExchange(url="/account")
    ApiResponse<AccountDto> getAccountEmail(@RequestParam("email") String email);
}
