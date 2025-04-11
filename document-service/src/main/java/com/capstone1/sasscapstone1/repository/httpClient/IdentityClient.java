package com.capstone1.sasscapstone1.repository.httpClient;

import com.capstone1.sasscapstone1.dto.AccountDto.AccountDto;
import com.capstone1.sasscapstone1.dto.AccountRatingDto.AccountRatingDto;
import com.capstone1.sasscapstone1.dto.response.ApiResponse;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.PostExchange;

import java.util.List;

public interface IdentityClient {
    @GetExchange(url="/account/{accountId}")
    ApiResponse<AccountDto> getAccountId(@PathVariable("accountId") long accountId);
    @GetExchange(url="/account")
    ApiResponse<AccountDto> getAccountEmail(@RequestParam("email") String email);
    @GetExchange(url="/stats/type")
    ApiResponse<Long> countStatsByRoleName(@RequestParam("roleName") String roleName);
    @PostExchange(url = "/cronjob/account/account-rated")
    ApiResponse<List<AccountRatingDto>> getAllAccountByRatingDocId(@RequestBody List<Long> accountIds,
                                                                   @RequestParam("docTitle") String docTitle,
                                                                   @RequestParam("docId") long docId);
    @PostExchange(url = "/cronjob/account/update/account-rated")
    ApiResponse<String> updateAccountRatedInRedis(@RequestBody List<Long> accountIds,
                                                                   @RequestParam("docTitle") String docTitle,
                                                                   @RequestParam("docId") long docId);

}
