package org.com.notificationservice.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import org.com.notificationservice.dto.response.AccountDto;
import org.com.notificationservice.service.RedisService;
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
public class WebsocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOrigins("http://localhost:5173")
                .withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic", "/queue", "/user");
        registry.setApplicationDestinationPrefixes("/app");
        registry.setUserDestinationPrefix("/user");
    }

//    @Override
//    public void configureClientInboundChannel(ChannelRegistration registration) {
//        registration.interceptors(new ChannelInterceptor() {
//            @Override
//            public Message<?> preSend(@Nonnull Message<?> message, @Nonnull MessageChannel channel) {
//                StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
//                if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
//                    try {
//                        String token = accessor.getFirstNativeHeader("token");
//                        if (token != null) {
//                            String json= (String) redisService.getData(token);
//                            AccountDto accountDto= objectMapper.readValue(json,AccountDto.class);
//                            List<GrantedAuthority> authorities = accountDto.getRoles().stream()
//                                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getName().toUpperCase()))
//                                    .collect(Collectors.toList());
//                            @SuppressWarnings("unchecked")
//                            Authentication authentication = new PreAuthenticatedAuthenticationToken(
//                                    accountDto.getAccountId(),
//                                    accountDto.getPassword(),
//                                    authorities
//                            );
//                            accessor.setUser(authentication);
//                        } else {
//                            throw new IllegalArgumentException("Token not found");
//                        }
//                    } catch (Exception e) {
//                        // Log lỗi và trả về null để từ chối kết nối
//                        System.err.println("WebSocket authentication error: " + e.getMessage());
//                        return null;
//                    }
//                }
//                return message;
//            }
//        });
//    }
}
