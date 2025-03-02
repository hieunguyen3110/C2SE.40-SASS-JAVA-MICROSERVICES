package org.com.apigateway.service;

import reactor.core.publisher.Mono;

public interface IdentityService {
    Mono<Boolean> verifyToken(String token);
}
