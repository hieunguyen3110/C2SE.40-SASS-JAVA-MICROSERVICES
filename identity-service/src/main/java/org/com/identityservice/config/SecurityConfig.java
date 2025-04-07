package org.com.identityservice.config;

import lombok.RequiredArgsConstructor;
import org.com.identityservice.filter.JwtAuthenticationFilter;
import org.com.identityservice.helpers.UserDetailServiceCustom;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final UserDetailServiceCustom userDetailServiceCustom;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private static final String[] PUBLIC_ENDPOINT={
            "/auth/login",
            "/auth/verify-token",
            "/auth/register",
            "/auth/refresh-token",
            "/auth/logout",
            "/auth/validate/reset-password",
            "/auth/update/new-password",
            "/auth/clear-token",
            "/ws/**",
            "/cronjob/**"
    };
    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity httpSecurity) throws Exception {
        AuthenticationManagerBuilder authenticationManagerBuilder= httpSecurity.getSharedObject(AuthenticationManagerBuilder.class);
        authenticationManagerBuilder.userDetailsService(userDetailServiceCustom).passwordEncoder(passwordEncoder());
        return authenticationManagerBuilder.build();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(request->{
                    request
                            .requestMatchers(PUBLIC_ENDPOINT).permitAll()
                            .requestMatchers(
                                    "/auth/change-password"
                            ).hasAnyRole("STUDENT","LECTURER")
                            .anyRequest().authenticated();
                });
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
