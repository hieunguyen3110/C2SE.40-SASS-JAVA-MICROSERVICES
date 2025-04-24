package org.com.identityservice.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.com.identityservice.dto.response.AccountDto;
import org.com.identityservice.enums.ErrorCode;
import org.com.identityservice.exception.ApiException;
import org.com.identityservice.repository.httpClient.DocumentClient;
import org.com.identityservice.repository.httpClient.ELearningClient;
import org.com.identityservice.repository.httpClient.RecommendationClient;
import org.com.identityservice.repository.httpClient.StudyGroupClient;
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
    @Bean
    @LoadBalanced
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }

    @Bean
    @LoadBalanced
    @Primary
    DocumentClient documentClient(WebClient.Builder builder){
        WebClient webClient = builder
                .baseUrl("lb://document-service/api/v1/document")
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
    @Bean
    @LoadBalanced
    ELearningClient eLearningClient(WebClient.Builder builder){
        WebClient webClient = builder
                .baseUrl("http://e-learning-service/api/v1/e-learning")
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
        return httpServiceProxyFactory.createClient(ELearningClient.class);
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
    @LoadBalanced
    StudyGroupClient studyGroupClient(WebClient.Builder builder){
        WebClient webClient = builder
                .baseUrl("http://study-group-service/api/v1/study-group")
                .defaultRequest(request->{
                    request.header("origin", "batch-service");
                })
                .build();
        HttpServiceProxyFactory httpServiceProxyFactory= HttpServiceProxyFactory.builderFor(WebClientAdapter.create(webClient)).build();
        return httpServiceProxyFactory.createClient(StudyGroupClient.class);
    }

}
