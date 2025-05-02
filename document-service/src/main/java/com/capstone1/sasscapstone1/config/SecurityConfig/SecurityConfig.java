package com.capstone1.sasscapstone1.config.SecurityConfig;

import com.capstone1.sasscapstone1.filter.JWTAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final JWTAuthenticationFilter jwtAuthenticationFilter;
    private static final String[] PUBLIC_ENDPOINT={
            "/user/count-stats",
            "/faculty/**",
            "/cronjob/**",
            "/actuator/**"
    };

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(request->{
                    request
                            .requestMatchers(PUBLIC_ENDPOINT).permitAll()
                            .requestMatchers(
                                    "/document/**",
                                    "/folder/**",
                                    "/download/**",
                                    "/chat-bot/**",
                                    "/user/**",
                                    "/subject/**"
                            ).hasAnyRole("STUDENT","LECTURE")
                            .requestMatchers(
                                    "/admin/**"
                            ).hasRole("ADMIN")
                            .anyRequest().authenticated();
                });
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
