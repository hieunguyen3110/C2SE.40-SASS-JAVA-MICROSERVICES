package org.com.batchservice.config;

import lombok.RequiredArgsConstructor;
import org.com.batchservice.repository.ChatbotClient;
import org.com.batchservice.repository.DocumentClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration
@RequiredArgsConstructor
public class WebClientConfig {
    @Bean
    @LoadBalanced
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }

    @Bean
    @LoadBalanced
    DocumentClient documentClient(WebClient.Builder builder){
        WebClient webClient = builder
                .baseUrl("http://document-service/api/v1/document")
                .defaultRequest(request->{
                    request.header("origin", "batch-service");
                })
                .build();
        HttpServiceProxyFactory httpServiceProxyFactory= HttpServiceProxyFactory.builderFor(WebClientAdapter.create(webClient)).build();
        return httpServiceProxyFactory.createClient(DocumentClient.class);
    }
    @Bean
    @LoadBalanced
    ChatbotClient chatbotClient(WebClient.Builder builder){
        WebClient webClient = builder
                .baseUrl("http://chatbot-service/api/v1/chatbot")
                .build();
        HttpServiceProxyFactory httpServiceProxyFactory= HttpServiceProxyFactory.builderFor(WebClientAdapter.create(webClient)).build();
        return httpServiceProxyFactory.createClient(ChatbotClient.class);
    }
}
