package org.com.websocketserver.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.com.websocketserver.dto.response.AccountDto;
import org.com.websocketserver.service.RedisService;
import org.springframework.context.annotation.Configuration;
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

import java.util.List;
import java.util.stream.Collectors;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
@Slf4j
public class WebsocketConfig implements WebSocketMessageBrokerConfigurer {
    private final ObjectMapper objectMapper;
    private final RedisService redisService;
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOrigins("http://localhost:5173", "http://localhost:8080", "http://dtuforyou.xyz", "https://dtuforyou.xyz")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic", "/queue", "/user");
        registry.setApplicationDestinationPrefixes("/app");
        registry.setUserDestinationPrefix("/user");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(@Nonnull Message<?> message, @Nonnull MessageChannel channel) {
                StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
                if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
                    try {
                        String token = accessor.getFirstNativeHeader("token");
                        if (token != null) {
                            String json = (String) redisService.getData(token);
                            if (json == null) {
                                throw new IllegalArgumentException("Invalid token");
                            }
                            AccountDto accountDto = objectMapper.readValue(json, AccountDto.class);

                            // Tạo danh sách quyền cho User
                            List<GrantedAuthority> authorities = accountDto.getRoles().stream()
                                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getName().toUpperCase()))
                                    .collect(Collectors.toList());

                            // Xác thực người dùng
                            Authentication authentication = new PreAuthenticatedAuthenticationToken(
                                    accountDto.getAccountId(), null, authorities);
                            accessor.setUser(authentication);
                        } else {
                            throw new IllegalArgumentException("Token not found");
                        }
                    } catch (Exception e) {
                        log.error("WebSocket authentication error: " + e.getMessage());
                        return null;
                    }
                }
                return message;
            }
        });
    }

}
