package org.com.identityservice.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.com.identityservice.dto.request.*;
import org.com.identityservice.dto.response.ApiResponse;
import org.com.identityservice.dto.response.AccountDto;
import org.com.identityservice.dto.response.LoginResponse;
import org.com.identityservice.entity.Account;
import org.com.identityservice.enums.ErrorCode;
import org.com.identityservice.exception.ApiException;
import org.com.identityservice.service.AuthenticationService;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthenticationController {
    private final AuthenticationService authenticationService;

    @PostMapping("/login")
    public ApiResponse<LoginResponse> handleLogin(@RequestBody LoginRequest loginRequest, HttpServletResponse response) throws Exception {
        return authenticationService.login(loginRequest,response);
    }
    @PostMapping("/register")
    public ApiResponse<String> handleRegister(@RequestBody RegisterRequest registerRequest) throws Exception {
        return authenticationService.register(registerRequest);
    }
    @GetMapping("/verify-token")
    public ApiResponse<Boolean> handleVerifyToken(@RequestParam("token") String token) throws Exception {
        return authenticationService.verifyToken(token);
    }
    @GetMapping("/refresh-token")
    public ApiResponse<String> handleRefreshToken(HttpServletRequest request, HttpServletResponse response) throws Exception {
        return authenticationService.refreshToken(request,response);
    }
    @GetMapping("/autoLogin")
    ApiResponse<LoginResponse> autoLogin(HttpServletRequest httpServletRequest) throws Exception {
        return authenticationService.autoLogin(httpServletRequest);
    }
    @GetMapping("/logout")
    public ApiResponse<String> handleLogout(HttpServletRequest request, HttpServletResponse response) throws Exception {
        return authenticationService.logout(request,response);
    }
    @PostMapping("/validate/reset-password")
    public ApiResponse<String> handleValidateResetPassword(@RequestBody SendOTPRequest request) throws Exception {
        return authenticationService.validateResetPassword(request);
    }

    @PostMapping("/update/new-password")
    public ApiResponse<String> handleSaveNewPassword(@RequestBody ResetPasswordRequest request) throws Exception {
        return authenticationService.resetPassword(request);
    }

    @PostMapping("/delete/clear-token")
    public ApiResponse<String> handleClearToken(@RequestBody ClearTokenRequest request) throws Exception{
        return authenticationService.clearToken(request);
    }
    @PutMapping("/change-password")
    public ApiResponse<String> handleChangePassword(@RequestBody ChangePasswordRequest request) throws Exception {
        Authentication authentication= SecurityContextHolder.getContext().getAuthentication();
        if(!(authentication instanceof AnonymousAuthenticationToken)){
            Account account= (Account) authentication.getPrincipal();
            return authenticationService.changePassword(request,account);
        }else{
            throw new ApiException(ErrorCode.FORBIDDEN.getStatusCode().value(),"You must be login");
        }
    }
}
