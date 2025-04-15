package org.com.batchservice.repository;

import org.com.batchservice.dto.response.AccountDto;
import org.com.batchservice.dto.response.ApiResponse;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.PostExchange;

import java.util.List;

public interface IdentityClient {
    @GetExchange("/cronjob/account/get-new-account")
    ApiResponse<List<AccountDto>> getAllNewAccountByDay();
    @GetExchange("/cronjob/account/get-new-account-active")
    ApiResponse<List<AccountDto>> getAllNewAccountByDayActive();
    @PostExchange("/cronjob/account/update-account-status")
    ApiResponse<String> updateAccountStatus(@RequestBody Long accountId);
}
