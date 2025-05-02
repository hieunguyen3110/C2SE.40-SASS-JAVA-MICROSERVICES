package com.capstone1.sasscapstone1.repository.httpClient;

import com.capstone1.sasscapstone1.dto.AccountDto.AccountDto;
import com.capstone1.sasscapstone1.dto.response.ApiResponse;
import com.capstone1.sasscapstone1.enums.ErrorCode;
import com.capstone1.sasscapstone1.exception.ApiException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.PostExchange;

import java.util.List;

public interface IdentityClient {
    @CircuitBreaker(name="identity-service", fallbackMethod = "fallbackGetAccountId")
    @Retry(name = "identity-service")
    @GetExchange(url="/account/{accountId}")
    ApiResponse<AccountDto> getAccountId(@PathVariable("accountId") long accountId);
    @CircuitBreaker(name="identity-service", fallbackMethod = "fallbackGetAccountEmail")
    @Retry(name = "identity-service")
    @GetExchange(url="/account")
    ApiResponse<AccountDto> getAccountEmail(@RequestParam("email") String email);
    @CircuitBreaker(name="identity-service", fallbackMethod = "fallbackCountStatsByRoleName")
    @Retry(name = "identity-service")
    @GetExchange(url="/stats/type")
    ApiResponse<Long> countStatsByRoleName(@RequestParam("roleName") String roleName);
    @CircuitBreaker(name="identity-service", fallbackMethod = "fallbackUpdateAccountRatedInRedis")
    @Retry(name = "identity-service")
    @PostExchange(url = "/cronjob/account/update/account-rated")
    void updateAccountRatedInRedis(@RequestBody List<Long> accountIds,
                                                                   @RequestParam("docTitle") String docTitle,
                                                                   @RequestParam("docId") long docId,
                                                  @RequestParam("rating") long rating);

    default ApiResponse<AccountDto> fallbackGetAccountId(long accountId, Throwable throwable) {
        throw new ApiException(ErrorCode.SERVICE_UNAVAILABLE.getStatusCode().value(), "Oops! Something went wrong, please try again later!");
    }
    default ApiResponse<AccountDto> fallbackGetAccountEmail(String email, Throwable throwable) {
        throw new ApiException(ErrorCode.SERVICE_UNAVAILABLE.getStatusCode().value(), "Oops! Something went wrong, please try again later!");
    }
    default ApiResponse<Long> fallbackCountStatsByRoleName(String roleName,Throwable throwable) {
        throw new ApiException(ErrorCode.SERVICE_UNAVAILABLE.getStatusCode().value(), "Oops! Something went wrong, please try again later!");
    }
    default void fallbackUpdateAccountRatedInRedis(List<Long> accountIds,String docTitle, long docId, long rating, Throwable throwable) {
        throw new ApiException(ErrorCode.SERVICE_UNAVAILABLE.getStatusCode().value(), "Oops! Something went wrong, please try again later!");
    }

}
