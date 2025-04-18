package org.com.studygroupservice.repository.httpClient;

import org.com.studygroupservice.dto.response.AccountDto;
import org.com.studygroupservice.dto.response.ApiResponse;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;

public interface IdentityClient {
    @GetExchange(url="/account/{accountId}")
    ApiResponse<AccountDto> getAccountId(@PathVariable("accountId") long accountId);
}
