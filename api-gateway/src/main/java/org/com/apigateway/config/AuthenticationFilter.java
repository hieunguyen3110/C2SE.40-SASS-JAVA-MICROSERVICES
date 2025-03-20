package org.com.apigateway.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHeaders;
import org.com.apigateway.exception.ApiException;
import org.com.apigateway.service.IdentityService;
import org.com.apigateway.service.RedisService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class AuthenticationFilter implements GlobalFilter, Ordered {

    private final String[] publicEndpoint= {
            "/identity/auth/login",
            "/identity/auth/register",
            "/identity/auth/refresh-token",
            "/identity/auth/logout",
            "/identity/auth/validate/reset-password",
            "/identity/auth/update/new-password",
            "/identity/auth/delete/clear-token",
            "/ws/**",
            "/eureka/web/**"
    };
    private final IdentityService identityService;
    private final RedisService redisService;
    @Value("${app.api-prefix}")
    private String apiPrefix;

    private boolean isPublishEndPoint(ServerHttpRequest request){
        String path = request.getURI().getPath();
        if(path.startsWith("/eureka") || path.startsWith("/ws")){
            return true;
        }
        return Arrays.stream(publicEndpoint).anyMatch(s -> path.startsWith(apiPrefix + s.replace("/**", "")));
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        log.info("Starter filter api gateway...");
        System.out.println("result: "+isPublishEndPoint(exchange.getRequest()));
        if(isPublishEndPoint(exchange.getRequest()))
            return chain.filter(exchange);

        List<String> authHeader= exchange.getRequest().getHeaders().get(HttpHeaders.AUTHORIZATION);
        if(CollectionUtils.isEmpty(authHeader)){
            throw new ApiException(HttpStatus.FORBIDDEN.value(),"You aren't permission.");
        }
        String token= authHeader.get(0).substring(7);
        return identityService.verifyToken(token).flatMap(res->{
            if(res) {
                String json= (String) redisService.getData(token);
                ServerHttpRequest modifiedRequest = exchange.getRequest().mutate()
                        .header("X-User-Info", json)
                        .build();
                return chain.filter(exchange.mutate().request(modifiedRequest).build());
            }else{
                return Mono.error(new ApiException(HttpStatus.FORBIDDEN.value(),"Token isn't valid."));
            }
        }).onErrorResume(ex -> {
            log.error("error verifying token {}",token);
            return Mono.error(new ApiException(HttpStatus.FORBIDDEN.value(), "Token verification failed."));
        });
    }

    @Override
    public int getOrder() {
        return -1;
    }
}
