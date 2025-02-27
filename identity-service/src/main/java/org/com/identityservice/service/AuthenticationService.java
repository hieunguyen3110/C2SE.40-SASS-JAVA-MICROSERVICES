package org.com.identityservice.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.com.identityservice.dto.request.*;
import org.com.identityservice.dto.response.ApiResponse;
import org.com.identityservice.dto.response.AccountDto;
import org.com.identityservice.entity.Account;

public interface AuthenticationService {
    ApiResponse<AccountDto> login(LoginRequest loginRequest, HttpServletResponse response) throws Exception;
    ApiResponse<AccountDto> autoLogin(HttpServletRequest request) throws Exception;
    ApiResponse<String> refreshToken(HttpServletRequest request, HttpServletResponse response) throws Exception;
    ApiResponse<String> logout(HttpServletRequest request, HttpServletResponse response) throws Exception;
    ApiResponse<String> register(RegisterRequest registerRequest) throws Exception;
    ApiResponse<String> validateResetPassword(SendOTPRequest request) throws Exception;
    ApiResponse<String> resetPassword(ResetPasswordRequest request) throws Exception;
    ApiResponse<String> clearToken(ClearTokenRequest request) throws Exception;
    ApiResponse<String> allowActiveAccount(String email) throws Exception;
    ApiResponse<String> changePassword(ChangePasswordRequest request, Account account) throws Exception;
}
