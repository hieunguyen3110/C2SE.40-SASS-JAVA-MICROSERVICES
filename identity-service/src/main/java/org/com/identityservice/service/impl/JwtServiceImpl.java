package org.com.identityservice.service.impl;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.com.identityservice.entity.Account;
import org.com.identityservice.service.JwtService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.*;
import java.util.function.Function;

@Service
public class JwtServiceImpl implements JwtService {
    @Value("${jwt.secret_key}")
    private String secretKey;
    @Value("${jwt.access_token_expires}")
    private String expAccessToken;
    @Value("${jwt.refresh_token_expires}")
    private String expRefreshToken;

    private SecretKey getSecretKey(){
        byte[] keys= Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keys);
    }

    private Claims ExtractClaimsAll(String token){
        return Jwts.parser()
                .verifyWith(getSecretKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private <T> T ExtractClaim(String token, Function<Claims,T> claimsResolver){
        final Claims claims= ExtractClaimsAll(token);
        return claimsResolver.apply(claims);
    }

    private String GeneratorToken(Account userDetails, Map<String, List<String>> extractClaim, Long expiresToken){
        try{
            Collection<? extends GrantedAuthority> manageRoles= userDetails.getAuthorities();
            List<String> roles= manageRoles.stream()
                    .map(GrantedAuthority::getAuthority)
                    .toList();
            extractClaim.put("roles",roles);
            extractClaim.put("accountId", List.of(String.valueOf(userDetails.getAccountId())));
            return Jwts.builder()
                    .signWith(getSecretKey())
                    .claims(extractClaim)
                    .subject(userDetails.getUsername())
                    .issuedAt(new Date(System.currentTimeMillis()))
                    .expiration(new Date(System.currentTimeMillis() + expiresToken))
                    .compact();
        }catch (Exception e){
            System.out.println(e.getMessage());
            return null;
        }
    }
    @Override
    public String GenerateAccessToken(Account userDetails){
        long expiresToken= Long.parseLong(expAccessToken);
        return GeneratorToken(userDetails,new HashMap<>(),expiresToken);
    }
    @Override
    public String GenerateRefreshToken(Account userDetails){
        long expiresToken= Long.parseLong(expRefreshToken);
        return GeneratorToken(userDetails, new HashMap<>(), expiresToken);
    }
    @Override
    public Boolean VerifyToken(String token){
        Claims validToken= ExtractClaimsAll(token);
        return validToken != null;
    }

    @Override
    public Claims DecodeToken(String token) {
        return ExtractClaimsAll(token);
    }

    private Date ExtractExpiration(String token){
        return ExtractClaim(token, Claims::getExpiration);
    }
    @Override
    public boolean isTokenExpiration(String token){
        return ExtractExpiration(token).before(new Date());
    }
    @Override
    public String ExtractUsername(String token){
        return ExtractClaim(token, Claims::getSubject);
    }
}
