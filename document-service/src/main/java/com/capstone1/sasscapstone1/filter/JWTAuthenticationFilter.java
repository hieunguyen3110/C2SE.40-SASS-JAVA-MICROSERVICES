package com.capstone1.sasscapstone1.filter;

import com.capstone1.sasscapstone1.enums.ErrorCode;
import com.capstone1.sasscapstone1.service.JwtService.JwtService;
import com.capstone1.sasscapstone1.service.UserDetailService.UserDetailServiceImpl;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JWTAuthenticationFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final UserDetailServiceImpl userDetailService;
    private String ExtractTokenFromHeader(HttpServletRequest request){
        String header= request.getHeader("Authorization");
        String token= null;
        if(StringUtils.hasText(header) && header.startsWith("Bearer ")){
            token = header.substring(7);
        }
        return token;
    }
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try{
            //authorization
            String token= ExtractTokenFromHeader(request);
            if(token!=null && !jwtService.isTokenExpiration(token)){
                String userName= jwtService.ExtractUsername(token);
                UserDetails userDetails= userDetailService.loadUserByUsername(userName);
                UsernamePasswordAuthenticationToken authenticationToken= new UsernamePasswordAuthenticationToken(userDetails,userDetails.getPassword(),userDetails.getAuthorities());
                authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            }
        }catch(ExpiredJwtException e){
//            response.setStatus(403);
//            response.getWriter().write("token expired");
            response.sendError(ErrorCode.FORBIDDEN.getStatusCode().value(),"token expired");
            return;
        }
        catch (Exception e){
//            response.setStatus(401);
//            response.getWriter().write("token isn't valid");
            response.sendError(ErrorCode.UNAUTHORIZED.getStatusCode().value(),"token isn't valid");
            return;
        }
        filterChain.doFilter(request,response);
    }
}
