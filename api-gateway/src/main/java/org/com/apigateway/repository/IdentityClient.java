package org.com.apigateway.repository;

import org.com.apigateway.dto.response.ApiResponse;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import reactor.core.publisher.Mono;
public interface IdentityClient {
    @GetExchange(url = "/auth/verify-token")
    Mono<ApiResponse<Boolean>> verifyTokenClient(@RequestParam("token") String token);
}
