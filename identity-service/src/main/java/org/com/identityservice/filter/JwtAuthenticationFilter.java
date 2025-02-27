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
import org.com.identityservice.service.JwtService;
import org.com.identityservice.service.RedisService;
import org.jetbrains.annotations.NotNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
        private final JwtService jwtService;
        private final RedisService redisService;
        private final ObjectMapper objectMapper;
        private String ExtractTokenFromHeader(HttpServletRequest request){
            String header= request.getHeader("Authorization");
            String token= null;
            if(StringUtils.hasText(header) && header.startsWith("Bearer ")){
                token = header.substring(7);
            }
            return token;
        }
        @Override
        protected void doFilterInternal(@NotNull HttpServletRequest request,
                                        @NotNull HttpServletResponse response,
                                        @NotNull FilterChain filterChain) throws ServletException, IOException, ApiException {
        try{
            //authorization
            String token= ExtractTokenFromHeader(request);
            if(token!=null && !jwtService.isTokenExpiration(token)){
                String json= (String) redisService.getData(token);
                AccountDto extractAccountFromRedis= objectMapper.readValue(json,AccountDto.class);
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
