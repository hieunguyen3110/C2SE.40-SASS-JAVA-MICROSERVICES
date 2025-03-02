package org.com.apigateway.service.Impl;

import lombok.RequiredArgsConstructor;
import org.com.apigateway.dto.response.ApiResponse;
import org.com.apigateway.exception.ApiException;
import org.com.apigateway.repository.IdentityClient;
import org.com.apigateway.service.IdentityService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class IdentityServiceImpl implements IdentityService {
    private final IdentityClient identityClient;
    @Override
    public Mono<Boolean> verifyToken(String token) {
        try{
            return identityClient.verifyTokenClient(token).map(ApiResponse::getData);
        }catch (Exception e){
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage());
        }
    }
}
