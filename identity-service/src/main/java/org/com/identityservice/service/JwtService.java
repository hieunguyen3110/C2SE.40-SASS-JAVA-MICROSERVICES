package org.com.identityservice.service;

import io.jsonwebtoken.Claims;
import org.com.identityservice.entity.Account;

public interface JwtService {
    String GenerateAccessToken(Account userDetails);
    String GenerateRefreshToken(Account userDetails);
    Boolean VerifyToken(String token);
    Claims DecodeToken(String token);
    boolean isTokenExpiration(String token);
    String ExtractUsername(String token);
}
