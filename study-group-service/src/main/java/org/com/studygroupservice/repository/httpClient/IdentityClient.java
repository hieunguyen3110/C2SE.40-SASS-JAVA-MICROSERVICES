package org.com.studygroupservice.repository.httpClient;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.com.studygroupservice.dto.response.AccountDto;
import org.com.studygroupservice.dto.response.ApiResponse;
import org.com.studygroupservice.enums.ErrorCode;
import org.com.studygroupservice.exception.ApiException;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.GetExchange;

import java.util.List;
import java.util.Set;

public interface IdentityClient {
    @GetExchange(url="/account/{accountId}")
    @CircuitBreaker(name = "identity-service", fallbackMethod = "fallbackGetAccountId")
    @Retry(name = "identity-service")
    ApiResponse<AccountDto> getAccountId(@PathVariable("accountId") long accountId);

    @GetExchange(url="/account/accounts/batch")
    @CircuitBreaker(name = "identity-service", fallbackMethod = "fallbackGetAccountIds")
    @Retry(name = "identity-service")
    ApiResponse<List<AccountDto>> getAccountsByIds(@RequestBody Set<Long> accountIds);

    default ApiResponse<AccountDto> fallbackGetAccountId(Throwable throwable){
        throw new ApiException(ErrorCode.SERVICE_UNAVAILABLE.getStatusCode().value(),"Oops! Something went wrong, please try identity service again later!");
    }
    default ApiResponse<List<AccountDto>> fallbackGetAccountIds(Throwable throwable){
        throw new ApiException(ErrorCode.SERVICE_UNAVAILABLE.getStatusCode().value(),"Oops! Something went wrong, please try identity service again later!");
    }
}
