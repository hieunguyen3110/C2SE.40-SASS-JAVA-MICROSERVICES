package org.com.identityservice.controller.Cronjob;

import lombok.RequiredArgsConstructor;
import org.com.identityservice.dto.response.AccountRatingDto;
import org.com.identityservice.dto.response.ApiResponse;
import org.com.identityservice.service.AccountService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/cronjob")
@RequiredArgsConstructor
public class CronjobController {
    private final AccountService accountService;

    @PostMapping("/account/account-rated")
    public ApiResponse<List<AccountRatingDto>> getAllAccountByRatingDocId(@RequestBody List<Long> accountIds,
                                                                          @RequestParam("docTitle") String docTitle,
                                                                          @RequestParam("docId") long docId) throws Exception {
        return accountService.getAccountByAccountIds(accountIds,docTitle,docId);
    }
    @PostMapping("/account/update/account-rated")
    public ApiResponse<String> updateAccountListInRedis(@RequestBody List<Long> accountIds,
                                                                          @RequestParam("docTitle") String docTitle,
                                                                          @RequestParam("docId") long docId) throws Exception {
        return accountService.updateListAccountRatingAtRedis(accountIds,docTitle,docId);
    }
}