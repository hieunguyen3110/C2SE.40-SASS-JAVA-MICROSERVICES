package org.com.studygroupservice.config;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.com.studygroupservice.dto.response.AccountDto;
import org.com.studygroupservice.exception.ApiException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebsocketConfig implements WebSocketMessageBrokerConfigurer {
    private final WebClient.Builder webClientBuilder;

    @Value("${allowed.origins:http://localhost:5173}")
    private String[] allowedOrigins;

    @Value("${identity.service.url:https://identity-service}")
    private String identityServiceUrl;

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws").setAllowedOrigins(allowedOrigins);
        registry.addEndpoint("/ws-sockjs").setAllowedOrigins(allowedOrigins).withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic", "/queue");
        registry.setApplicationDestinationPrefixes("/app");
        registry.setUserDestinationPrefix("/user");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(@NonNull Message<?> message, @NonNull MessageChannel channel) {
                StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
                if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
                    try {
                        String token = accessor.getFirstNativeHeader("token");
                        if (token == null || token.isEmpty()) {
                            throw new ApiException(HttpStatus.UNAUTHORIZED.value(), "Token is required");
                        }

                        AccountDto accountDto = webClientBuilder.build()
                                .get()
                                .uri(identityServiceUrl + "/validate-token?token=" + token)
                                .retrieve()
                                .onStatus(HttpStatus.UNAUTHORIZED::equals, response ->
                                        Mono.error(new ApiException(HttpStatus.UNAUTHORIZED.value(), "Invalid or expired token")))
                                .bodyToMono(AccountDto.class)
                                .timeout(Duration.ofSeconds(3))
                                .block();

                        if (accountDto == null) {
                            throw new ApiException(HttpStatus.UNAUTHORIZED.value(), "Invalid or expired token");
                        }

                        List<GrantedAuthority> authorities = accountDto.getRoles().stream()
                                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getName().toUpperCase()))
                                .collect(Collectors.toList());

                        Authentication authentication = new PreAuthenticatedAuthenticationToken(
                                accountDto.getAccountId(), null, authorities);
                        accessor.setUser(authentication);
                    } catch (ApiException e) {
                        log.error("WebSocket authentication failed: {}", e.getMessage());
                        accessor.setLeaveMutable(true);
                        throw e;
                    } catch (Exception e) {
                        log.error("Unexpected WebSocket authentication error", e);
                        accessor.setLeaveMutable(true);
                        throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Unexpected authentication error");
                    }
                }
                return message;
            }
        });
    }
}