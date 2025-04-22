package org.com.studygroupservice.repository.httpClient;

import org.com.studygroupservice.dto.response.AccountDto;
import org.com.studygroupservice.dto.response.ApiResponse;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.PostExchange;

import java.util.List;
import java.util.Set;

public interface IdentityClient {
    @GetExchange(url="/account/{accountId}")
    ApiResponse<AccountDto> getAccountId(@PathVariable("accountId") long accountId);

    @GetExchange(url="/account/accounts/batch")
    ApiResponse<List<AccountDto>> getAccountsByIds(@RequestBody Set<Long> accountIds);
}
