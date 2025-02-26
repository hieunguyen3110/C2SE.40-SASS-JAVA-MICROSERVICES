package com.capstone1.sasscapstone1.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class CookieUtils {
    @Value("${jwt.access_token_expires}")
    private String expiresAccessToken;
    @Value("${jwt.refresh_token_expires}")
    private String expiresRefreshToken;
    public Cookie addAttributeForCookie(Cookie cookie, int expiresInSeconds){
        cookie.setMaxAge(Math.max(expiresInSeconds, 0));
        cookie.setPath("/");
//        cookie.setAttribute("SameSite","None");
//        cookie.setSecure(true);
        return cookie;
    }
    public void generatorTokenCookie(HttpServletResponse response, String accessToken, String refreshToken) {
        Cookie accessTokenCookie= new Cookie("accessToken",accessToken);
        Cookie refreshTokenCookie= new Cookie("refreshToken",refreshToken);
        if(accessToken==null || refreshToken==null){
            accessTokenCookie=addAttributeForCookie(accessTokenCookie, 0);
            refreshTokenCookie= addAttributeForCookie(refreshTokenCookie,0);
        }else{
            accessTokenCookie=addAttributeForCookie(accessTokenCookie, Integer.parseInt(expiresAccessToken)/1000);
            refreshTokenCookie= addAttributeForCookie(refreshTokenCookie,Integer.parseInt(expiresRefreshToken)/1000);
        }
        refreshTokenCookie.setHttpOnly(true);
        response.addCookie(accessTokenCookie);
        response.addCookie(refreshTokenCookie);
    }
    public void saveAccessTokenCookie(HttpServletResponse response, String accessToken){
        Cookie accessTokenCookie= new Cookie("accessToken", accessToken);
        accessTokenCookie=addAttributeForCookie(accessTokenCookie, Integer.parseInt(expiresAccessToken)/1000);
        response.addCookie(accessTokenCookie);
    }
    public void removeCookieOAuth2(HttpServletResponse response, String userId, String accessToken,String refreshToken){
        Cookie accessTokenCookie= new Cookie("accessToken", accessToken);
        Cookie refreshTokenCookie= new Cookie("refreshToken",refreshToken);
        Cookie userIdCookie= new Cookie("userId", userId);
        accessTokenCookie=addAttributeForCookie(accessTokenCookie, 0);
        userIdCookie=addAttributeForCookie(userIdCookie, 0);
        refreshTokenCookie= addAttributeForCookie(refreshTokenCookie,0);
        refreshTokenCookie.setHttpOnly(true);
        userIdCookie.setHttpOnly(true);
        response.addCookie(accessTokenCookie);
        response.addCookie(userIdCookie);
    }
    public void addSubIDCookie(HttpServletResponse response, String sub){
        Cookie subId= new Cookie("subId", sub);
        subId= addAttributeForCookie(subId,Integer.parseInt(expiresRefreshToken));
        subId.setHttpOnly(true);
        response.addCookie(subId);
    }
}
