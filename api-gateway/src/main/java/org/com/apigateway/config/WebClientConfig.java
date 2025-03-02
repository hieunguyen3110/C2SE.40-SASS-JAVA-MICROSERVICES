package org.com.apigateway.config;

import org.com.apigateway.repository.IdentityClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration
public class WebClientConfig {

    @Bean
    @LoadBalanced
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }

    @Bean
    WebClient webClient(WebClient.Builder builder){
        return builder
                .baseUrl("http://IDENTITY-SERVICE/api/v1/identity")
                .build();
    }

    @Bean
    IdentityClient identityClient(WebClient.Builder builder){
        WebClient webClient = builder.baseUrl("http://IDENTITY-SERVICE/api/v1/identity").build();
        HttpServiceProxyFactory httpServiceProxyFactory= HttpServiceProxyFactory.builderFor(WebClientAdapter.create(webClient)).build();
        return httpServiceProxyFactory.createClient(IdentityClient.class);
    }
}
