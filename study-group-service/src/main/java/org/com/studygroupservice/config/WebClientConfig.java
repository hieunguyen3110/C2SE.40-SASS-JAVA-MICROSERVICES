package org.com.studygroupservice.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.com.studygroupservice.dto.response.AccountDto;
import org.com.studygroupservice.enums.ErrorCode;
import org.com.studygroupservice.exception.ApiException;
import org.com.studygroupservice.repository.httpClient.DocumentClient;
import org.com.studygroupservice.repository.httpClient.IdentityClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

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
    IdentityClient identityClient(WebClient.Builder builder){
        WebClient webClient = builder
                .baseUrl("http://identity-service/api/v1/identity")
                .defaultRequest(request->{
                    Authentication authentication= SecurityContextHolder.getContext().getAuthentication();
                    if(authentication != null && !(authentication instanceof AnonymousAuthenticationToken)){
                        try{
                            AccountDto accountDto= (AccountDto) authentication.getPrincipal();
                            String userInfoJson = objectMapper.writeValueAsString(accountDto);
                            String base64Json = Base64.getEncoder().encodeToString(userInfoJson.getBytes(StandardCharsets.UTF_8));
                            request.header("X-User-Info", base64Json);
                        }catch (JsonProcessingException e){
                            throw new ApiException(ErrorCode.INTERNAL_SERVER_ERROR.getStatusCode().value(),"Error serializing X-User-Info");
                        }
                    }
                })
                .build();
        HttpServiceProxyFactory httpServiceProxyFactory= HttpServiceProxyFactory.builderFor(WebClientAdapter.create(webClient)).build();
        return httpServiceProxyFactory.createClient(IdentityClient.class);
    }
    @Bean
    @LoadBalanced
    DocumentClient documentClient(WebClient.Builder builder){
        WebClient webClient = builder
                .baseUrl("http://document-service/api/v1/document")
                .defaultRequest(request->{
                    Authentication authentication= SecurityContextHolder.getContext().getAuthentication();
                    if(authentication != null && !(authentication instanceof AnonymousAuthenticationToken)){
                        try{
                            AccountDto accountDto= (AccountDto) authentication.getPrincipal();
                            String userInfoJson = objectMapper.writeValueAsString(accountDto);
                            String base64Json = Base64.getEncoder().encodeToString(userInfoJson.getBytes(StandardCharsets.UTF_8));
                            request.header("X-User-Info", base64Json);
                        }catch (JsonProcessingException e){
                            throw new ApiException(ErrorCode.INTERNAL_SERVER_ERROR.getStatusCode().value(),"Error serializing X-User-Info");
                        }
                    }
                })
                .build();
        HttpServiceProxyFactory httpServiceProxyFactory= HttpServiceProxyFactory.builderFor(WebClientAdapter.create(webClient)).build();
        return httpServiceProxyFactory.createClient(DocumentClient.class);
    }
}
