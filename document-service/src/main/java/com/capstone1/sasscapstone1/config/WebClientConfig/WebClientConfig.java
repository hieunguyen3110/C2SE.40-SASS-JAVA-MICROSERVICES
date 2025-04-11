package com.capstone1.sasscapstone1.config.WebClientConfig;

import com.capstone1.sasscapstone1.dto.AccountDto.AccountDto;
import com.capstone1.sasscapstone1.enums.ErrorCode;
import com.capstone1.sasscapstone1.exception.ApiException;
import com.capstone1.sasscapstone1.repository.httpClient.IdentityClient;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration
@RequiredArgsConstructor
public class WebClientConfig {
    private final ObjectMapper objectMapper;

    @Bean
    @LoadBalanced
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }

    @Bean
    @LoadBalanced
    @Primary
    IdentityClient identityClient(WebClient.Builder builder){
        WebClient webClient = builder
                .baseUrl("http://identity-service/api/v1/identity")
                .defaultRequest(request->{
                    Authentication authentication= SecurityContextHolder.getContext().getAuthentication();
                    if(authentication != null && !(authentication instanceof AnonymousAuthenticationToken)){
                        try{
                            AccountDto accountDto= (AccountDto) authentication.getPrincipal();
                            String userInfoJson = objectMapper.writeValueAsString(accountDto);
                            request.header("X-User-Info", userInfoJson);
                        }catch (JsonProcessingException e){
                            throw new ApiException(ErrorCode.INTERNAL_SERVER_ERROR.getStatusCode().value(),"Error serializing X-User-Info");
                        }
                    }
                })
                .build();
        HttpServiceProxyFactory httpServiceProxyFactory= HttpServiceProxyFactory.builderFor(WebClientAdapter.create(webClient)).build();
        return httpServiceProxyFactory.createClient(IdentityClient.class);
    }

    @Bean(name = "identityClientWithoutSecurity")
    @LoadBalanced
    public IdentityClient identityClientWithoutSecurity(WebClient.Builder builder) {
        WebClient webClient = builder
                .baseUrl("http://identity-service/api/v1/identity")
                .defaultRequest(request->{
                    request.header("origin", "batch-service");
                })
                .build();

        HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(WebClientAdapter.create(webClient)).build();
        return factory.createClient(IdentityClient.class);
    }

    @Bean
    @LoadBalanced
    public WebClient studyGroupWebClient(WebClient.Builder builder) {
        return builder
                .baseUrl("http://study-group-service")
                .build();
    }
}
