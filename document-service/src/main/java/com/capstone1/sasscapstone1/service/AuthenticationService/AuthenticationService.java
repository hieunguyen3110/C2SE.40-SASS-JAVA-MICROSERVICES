package com.capstone1.sasscapstone1.service.AuthenticationService;

import com.capstone1.sasscapstone1.dto.LoginDto.LoginDto;
import com.capstone1.sasscapstone1.dto.response.ApiResponse;
import com.capstone1.sasscapstone1.entity.Account;
import com.capstone1.sasscapstone1.request.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public interface AuthenticationService {
    ApiResponse<LoginDto> login(LoginRequest loginRequest, HttpServletResponse response) throws Exception;
    ApiResponse<LoginDto> autoLogin(HttpServletRequest request) throws Exception;
    ApiResponse<String> refreshToken(HttpServletRequest request,HttpServletResponse response) throws Exception;
    ApiResponse<String> logout(HttpServletRequest request, HttpServletResponse response) throws Exception;
    ApiResponse<String> register(RegisterRequest registerRequest) throws Exception;
    ApiResponse<String> validateResetPassword(SendOTPRequest request) throws Exception;
    ApiResponse<String> resetPassword(ResetPasswordRequest request) throws Exception;
    ApiResponse<String> clearToken(ClearTokenRequest request) throws Exception;
    ApiResponse<String> allowActiveAccount(String email) throws Exception;
    ApiResponse<String> changePassword(ChangePasswordRequest request, Account account) throws Exception;
}
