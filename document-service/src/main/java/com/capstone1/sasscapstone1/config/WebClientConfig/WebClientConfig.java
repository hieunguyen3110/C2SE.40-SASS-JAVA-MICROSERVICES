package com.capstone1.sasscapstone1.config.WebClientConfig;

import com.capstone1.sasscapstone1.dto.AccountDto.AccountDto;
import com.capstone1.sasscapstone1.enums.ErrorCode;
import com.capstone1.sasscapstone1.exception.ApiException;
import com.capstone1.sasscapstone1.repository.httpClient.ChatbotClient;
import com.capstone1.sasscapstone1.repository.httpClient.IdentityClient;
import com.capstone1.sasscapstone1.repository.httpClient.RecommendationClient;
import com.capstone1.sasscapstone1.repository.httpClient.StudyGroupClient;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
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

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Configuration
@RequiredArgsConstructor
public class WebClientConfig {
    private final ObjectMapper objectMapper;
    @Value("${recommendation.url}")
    private String recommendationUrl;
    @Value("${chatbot.url}")
    private String chatbotUrl;

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
    public StudyGroupClient studyGroupClient(WebClient.Builder builder) {
        WebClient webClient = builder
                .baseUrl("http://study-group-service/api/v1/study-group")
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
        return httpServiceProxyFactory.createClient(StudyGroupClient.class);
    }
    @Bean
    public RecommendationClient recommendationClient() {
        WebClient webClient = WebClient.builder()
                .baseUrl(recommendationUrl)
                .build();
        HttpServiceProxyFactory factory = HttpServiceProxyFactory
                .builderFor(WebClientAdapter.create(webClient))
                .build();

        return factory.createClient(RecommendationClient.class);
    }
    @Bean
    public ChatbotClient chatbotClient() {
        WebClient webClient = WebClient.builder()
                .baseUrl(chatbotUrl)
                .build();
        HttpServiceProxyFactory factory = HttpServiceProxyFactory
                .builderFor(WebClientAdapter.create(webClient))
                .build();

        return factory.createClient(ChatbotClient.class);
    }
}
