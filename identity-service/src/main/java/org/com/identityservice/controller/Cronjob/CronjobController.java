package org.com.identityservice.controller.Cronjob;

import lombok.RequiredArgsConstructor;
import org.com.identityservice.dto.response.AccountDto;
import org.com.identityservice.dto.response.AccountRatingDto;
import org.com.identityservice.dto.response.ApiResponse;
import org.com.identityservice.helpers.CreateApiResponse;
import org.com.identityservice.service.AccountService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/cronjob")
@RequiredArgsConstructor
public class CronjobController {
    private final AccountService accountService;

//    @PostMapping("/account/account-rated")
//    public ApiResponse<List<AccountRatingDto>> getAllAccountByRatingDocId(@RequestBody List<Long> accountIds,
//                                                                          @RequestParam("docTitle") String docTitle,
//                                                                          @RequestParam("docId") long docId) throws Exception {
//        return accountService.getAccountByAccountIds(accountIds,docTitle,docId);
//    }
    @PostMapping("/account/update/account-rated")
    public ApiResponse<String> updateAccountListInRedis(@RequestBody List<Long> accountIds,
                                                                          @RequestParam("docTitle") String docTitle,
                                                                          @RequestParam("docId") long docId,
                                                        @RequestParam("rating") long rating) throws Exception {
        return accountService.updateListAccountRatingAtRedis(accountIds,docTitle,docId, rating);
    }
    @GetMapping("/account/get-new-account")
    public ApiResponse<List<AccountDto>> getAllNewAccountByDay() throws Exception {
        return CreateApiResponse.createResponse(accountService.getAllNewUserByDay(),false);
    }
    @GetMapping("/account/get-new-account-active")
    public ApiResponse<List<AccountDto>> getAllNewAccountByDayActive() throws Exception {
        return CreateApiResponse.createResponse(accountService.getAllNewUserByDayIsActiveIsTrue(),false);
    }
    @PostMapping("/account/update-account-status")
    public ApiResponse<String> updateStatusAccount(@RequestBody Long accountId) throws Exception {
        return CreateApiResponse.createResponse(accountService.approveNewUsers(accountId),false);
    }
}