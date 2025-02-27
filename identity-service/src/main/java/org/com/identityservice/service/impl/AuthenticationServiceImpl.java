package org.com.identityservice.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.persistence.EntityManager;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.com.identityservice.dto.request.*;
import org.com.identityservice.dto.response.AccountDto;
import org.com.identityservice.dto.response.ApiResponse;
import org.com.identityservice.entity.Account;
import org.com.identityservice.entity.Role;
import org.com.identityservice.enums.ErrorCode;
import org.com.identityservice.exception.ApiException;
import org.com.identityservice.helpers.CookieUtils;
import org.com.identityservice.helpers.CreateApiResponse;
import org.com.identityservice.mapper.AccountMapper;
import org.com.identityservice.repository.AccountRepository;
import org.com.identityservice.repository.RoleRepository;
import org.com.identityservice.service.AuthenticationService;
import org.com.identityservice.service.JwtService;
import org.com.identityservice.service.RedisService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {
    private final AccountRepository accountRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final CookieUtils cookieUtils;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final EntityManager entityManager;
    private final RedisService redisService;
    private final ObjectMapper objectMapper;
    @Value("${jwt.access_token_expires}")
    private String accessTokenExpires;
    @Value("${jwt.refresh_token_expires}")
    private String refreshTokenExpires;
    private boolean checkExistEmail(String email){
        Optional<Account> existEmail= accountRepository.findAccountByEmail(email);
        return existEmail.isPresent();
    }
    @Override
    public ApiResponse<AccountDto> login(LoginRequest loginRequest, HttpServletResponse response) throws Exception {
        try{
            Authentication authentication= authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getEmail().toLowerCase(),loginRequest.getPassword()));
            if(!(authentication.getPrincipal() instanceof AnonymousAuthenticationToken)){
                Account account= (Account) authentication.getPrincipal();
                String accessToken= jwtService.GenerateAccessToken(account);
                String refreshToken= jwtService.GenerateRefreshToken(account);
                cookieUtils.generatorTokenCookie(response,accessToken,"user"+account.getAccountId());
                var loginResponse= AccountMapper.mapToAccountDto(account);
                String extractToJson=objectMapper.writeValueAsString(loginResponse);
                redisService.saveData("user"+account.getAccountId(),refreshToken,Long.parseLong(refreshTokenExpires)/1000);
                redisService.saveData(accessToken,extractToJson,Long.parseLong(accessTokenExpires)/1000);
                return CreateApiResponse.createResponse(loginResponse,false);
            }else{
                throw new ApiException(ErrorCode.BAD_REQUEST.getStatusCode().value(),"Email or password incorrect");
            }
        }catch (BadCredentialsException e){
            throw new ApiException(ErrorCode.BAD_REQUEST.getStatusCode().value(),"Email or password incorrect");
        }catch (ApiException e) {
            throw new ApiException(e.getCode(), e.getMessage());
        }catch (Exception e){
            throw new Exception(e.getMessage());
        }
    }

    @Override
    public ApiResponse<AccountDto> autoLogin(HttpServletRequest request) throws Exception {
        try{
            Cookie[] cookies= request.getCookies();
            String accessToken = null;
            if(cookies==null){
                throw new ApiException(ErrorCode.FORBIDDEN.getStatusCode().value(),"Token isn't valid");
            }
            for(Cookie cookie : cookies){
                if (cookie.getName().equals("accessToken")) {
                    accessToken = cookie.getValue();
                    break;
                }
            }
            if (accessToken==null){
                throw new ApiException(ErrorCode.FORBIDDEN.getStatusCode().value(),"Access token is expires");
            }
            Authentication authentication= SecurityContextHolder.getContext().getAuthentication();
            if(!(authentication instanceof AnonymousAuthenticationToken)){
                Account loadUser= (Account) authentication.getPrincipal();
                AccountDto accountDto= AccountMapper.mapToAccountDto(loadUser);
                return CreateApiResponse.createResponse(accountDto,false);
            }else{
                throw new ApiException(ErrorCode.FORBIDDEN.getStatusCode().value(),"Token isn't valid");
            }
        }catch (ApiException e){
            throw new ApiException(e.getCode(), e.getMessage());
        }catch (Exception e){
            throw new Exception(e.getMessage());
        }
    }

    @Override
    public ApiResponse<String> refreshToken(HttpServletRequest request ,HttpServletResponse response) throws Exception {
        Cookie[] cookies= request.getCookies();
        String userId = null;
        try{
            if(cookies==null){
                throw new ApiException(ErrorCode.UNAUTHORIZED.getStatusCode().value(),"refresh token is not found");
            }
            for(Cookie cookie : cookies){
                if (cookie.getName().equals("userId")){
                    userId=cookie.getValue();
                    break;
                }
            }
            if(userId == null){
                throw new ApiException(ErrorCode.UNAUTHORIZED.getStatusCode().value(),"refresh token is not found");
            }
            String refreshToken= (String) redisService.getData(userId);
            String userName= jwtService.ExtractUsername(refreshToken);
            if(userName != null){
                Optional<Account> findAccount= accountRepository.findAccountByEmail(userName.toLowerCase());
                if(findAccount.isPresent()){
                    Account account= findAccount.get();
                    String newAccessToken= jwtService.GenerateAccessToken(account);
                    var accountDto= AccountMapper.mapToAccountDto(account);
                    String extractToJson=objectMapper.writeValueAsString(accountDto);
                    redisService.saveData(newAccessToken,extractToJson,Long.parseLong(accessTokenExpires)/1000);
                    cookieUtils.generatorTokenCookie(response,newAccessToken,"user"+account.getAccountId());
                    return CreateApiResponse.createResponse("Refresh token success",false);
                }else{
                    throw new ApiException(ErrorCode.BAD_REQUEST.getStatusCode().value(), "Refresh token not valid");
                }
            }
            throw new ApiException(ErrorCode.UNAUTHORIZED.getStatusCode().value(), "Refresh token is expires");
        }catch (ApiException e){
            throw new ApiException(e.getCode(), e.getMessage());
        }catch (ExpiredJwtException e){
            cookieUtils.generatorTokenCookie(response,null,null);
            throw new ApiException(ErrorCode.UNAUTHORIZED.getStatusCode().value(), "Refresh token is expires");
        }catch (Exception e){
            throw new Exception(e.getMessage());
        }
    }

    @Override
    public ApiResponse<String> logout(HttpServletRequest request, HttpServletResponse response) throws Exception {
        try{
            Cookie[] cookies= request.getCookies();
            String userId= null;
            String accessToken= null;
            if(cookies ==null){
                return CreateApiResponse.createResponse("Logout success",false);
            }
            for(Cookie cookie : cookies){
                if (cookie.getName().equals("userId")){
                    userId= cookie.getValue();
                }
                if(cookie.getName().equals("accessToken")){
                    accessToken=cookie.getValue();
                }
            }
            if(userId == null){
                return CreateApiResponse.createResponse("Logout success",false);
            }
            if(accessToken!=null){
                redisService.deleteData(accessToken);
            }
            redisService.deleteData(userId);
            cookieUtils.generatorTokenCookie(response,null,null);
            return CreateApiResponse.createResponse("Logout success",false);
        }catch (Exception e){
            throw new Exception(e.getMessage());
        }
    }

    @Override
    public ApiResponse<String> register(RegisterRequest registerRequest) throws Exception {
        try{
            if(checkExistEmail(registerRequest.getEmail())){
                throw new ApiException(ErrorCode.BAD_REQUEST.getStatusCode().value(),"Email is exist");
            }
            Role role= roleRepository.findRoleByName(registerRequest.getRoleName());
            if(role==null){
                throw new Exception("Role not found");
            }
            Account account= Account.builder()
                    .email(registerRequest.getEmail().toLowerCase())
                    .password(passwordEncoder.encode(registerRequest.getPassword()))
                    .roles(Collections.singleton(role))
                    .isActive(false)
                    .isDeleted(false)
                    .build();
            accountRepository.save(account);
            return CreateApiResponse.createResponse("Register account is successful",true);
        }catch (ApiException ex){
            throw new ApiException(ex.getCode(),ex.getMessage());
        }catch (Exception e){
            throw new Exception(e.getMessage());
        }
    }
    @Override
    public ApiResponse<String> validateResetPassword(SendOTPRequest request) throws Exception {
        try{
            if(request.getOtp() != null){
                redisService.saveData(request.getEmail()+request.getOtp(),request.getOtp(),600);
                return CreateApiResponse.createResponse("Send request reset password success",false);
            }else{
                throw new Exception("Code isn't valid");
            }
        }catch (Exception e){
            throw new Exception(e.getMessage());
        }
    }
    @Override
    @Transactional
    public ApiResponse<String> resetPassword(ResetPasswordRequest request) throws Exception {
        try{
            if(request!=null){
                Account findAccount = accountRepository.findAccountByEmail(request.getEmail().toLowerCase()).orElseThrow(()->new ApiException(ErrorCode.BAD_REQUEST.getStatusCode().value(),"Account not found"));
                findAccount.setPassword(passwordEncoder.encode(request.getPassword()));
                return CreateApiResponse.createResponse("Reset password is success",false);
            }else{
                throw new ApiException(ErrorCode.BAD_REQUEST.getStatusCode().value(),"token isn't valid");
            }
        }catch (Exception e){
            throw new Exception(e.getMessage());
        }
    }

    @Override
    @Transactional
    public ApiResponse<String> clearToken(ClearTokenRequest request) throws Exception {
        try{
            redisService.deleteData(request.getEmail()+request.getOtp());
            return CreateApiResponse.createResponse("Clear token is success",false);
        }catch (Exception e){
            throw new Exception(e.getMessage());
        }
    }

    @Override
    public ApiResponse<String> allowActiveAccount(String email) throws Exception {
        try{
            Authentication authentication= SecurityContextHolder.getContext().getAuthentication();
            if(!(authentication instanceof AnonymousAuthenticationToken)){
                Optional<Account> findAccount= accountRepository.findAccountByEmail(email.toLowerCase());
                if (findAccount.isPresent()){
                    Account account= findAccount.get();
                    account.setIsActive(true);
                    accountRepository.save(account);
                    return CreateApiResponse.createResponse("Update account successful",false);
                }else{
                    throw new ApiException(ErrorCode.BAD_REQUEST.getStatusCode().value(),"Account not found by email: "+email);
                }
            }else{
                throw new ApiException(ErrorCode.FORBIDDEN.getStatusCode().value(),"Account not allowed");
            }
        }catch (Exception e){
            throw new Exception(e.getMessage());
        }
    }

    @Override
    @Transactional
    public ApiResponse<String> changePassword(ChangePasswordRequest request, Account account) throws Exception {
        try{
            if(request.getNewPassword()==null || request.getOldPassword()==null){
                throw new ApiException(ErrorCode.BAD_REQUEST.getStatusCode().value(),"Missing required parameter");
            }
            if(passwordEncoder.matches(request.getOldPassword(),account.getPassword())){
                Account entityAccount= entityManager.merge(account);
                entityAccount.setPassword(passwordEncoder.encode(request.getNewPassword()));
                accountRepository.save(entityAccount);
                return CreateApiResponse.createResponse("Change password successful",false);
            }else{
                throw new Exception("Old password incorrect");
            }
        }catch (Exception e){
            throw new Exception(e.getMessage());
        }
    }
}
