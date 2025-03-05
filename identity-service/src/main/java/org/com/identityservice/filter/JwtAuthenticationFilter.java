package org.com.identityservice.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.com.identityservice.dto.response.AccountDto;
import org.com.identityservice.entity.Account;
import org.com.identityservice.enums.ErrorCode;
import org.com.identityservice.exception.ApiException;
import org.com.identityservice.mapper.AccountMapper;
import org.jetbrains.annotations.NotNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
        private final ObjectMapper objectMapper;
        @Override
        protected void doFilterInternal(@NotNull HttpServletRequest request,
                                        @NotNull HttpServletResponse response,
                                        @NotNull FilterChain filterChain) throws ServletException, IOException, ApiException {
        try{
            String userInfoJson= request.getHeader("X-User-Info");
            if(userInfoJson != null){
                AccountDto extractAccountFromRedis= objectMapper.readValue(userInfoJson,AccountDto.class);
                Account account= AccountMapper.mapToAccount(extractAccountFromRedis);
                UsernamePasswordAuthenticationToken authenticationToken= new UsernamePasswordAuthenticationToken(account,account.getPassword(),account.getAuthorities());
                authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            }
        }catch(ExpiredJwtException e){
            throw new ApiException(ErrorCode.FORBIDDEN.getStatusCode().value(),"token expired");
        }
        catch (Exception e){
            throw new ApiException(ErrorCode.INTERNAL_SERVER_ERROR.getStatusCode().value(),"INTERNAL SERVER ERROR");
        }
        filterChain.doFilter(request,response);
    }
}
