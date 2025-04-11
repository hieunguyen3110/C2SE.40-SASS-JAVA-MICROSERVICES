package org.com.batchservice.config;

import lombok.RequiredArgsConstructor;
import org.com.batchservice.constant.AppConstant;
import org.com.batchservice.repository.ChatbotClient;
import org.com.batchservice.repository.DocumentClient;
import org.com.batchservice.repository.IdentityClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration
@RequiredArgsConstructor
public class WebClientConfig {
    @Value("${chatbot.url}")
    private String chatBotUrl;
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
                    request.header("origin", AppConstant.SERVICE_NAME);
                })
                .build();
        HttpServiceProxyFactory httpServiceProxyFactory= HttpServiceProxyFactory.builderFor(WebClientAdapter.create(webClient)).build();
        return httpServiceProxyFactory.createClient(DocumentClient.class);
    }
    @Bean
    @LoadBalanced
    IdentityClient identityClient(WebClient.Builder builder){
        WebClient webClient = builder
                .baseUrl("http://identity-service/api/v1/identity")
                .defaultRequest(request->{
                    request.header("origin", AppConstant.SERVICE_NAME);
                })
                .build();
        HttpServiceProxyFactory httpServiceProxyFactory= HttpServiceProxyFactory.builderFor(WebClientAdapter.create(webClient)).build();
        return httpServiceProxyFactory.createClient(IdentityClient.class);
    }
    @Bean
    public ChatbotClient chatbotClient() {
        WebClient webClient = WebClient.builder()
                .baseUrl(chatBotUrl)
                .build();
        HttpServiceProxyFactory factory = HttpServiceProxyFactory
                .builderFor(WebClientAdapter.create(webClient))
                .build();

        return factory.createClient(ChatbotClient.class);
    }
}
