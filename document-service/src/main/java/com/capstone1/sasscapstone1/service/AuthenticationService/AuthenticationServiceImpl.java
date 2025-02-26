package com.capstone1.sasscapstone1.service.AuthenticationService;

import com.capstone1.sasscapstone1.dto.AccountStatisticsDto.AccountStatisticsDto;
import com.capstone1.sasscapstone1.dto.LoginDto.LoginDto;
import com.capstone1.sasscapstone1.dto.response.ApiResponse;
import com.capstone1.sasscapstone1.entity.Account;
import com.capstone1.sasscapstone1.entity.BlackList;
import com.capstone1.sasscapstone1.entity.Role;
import com.capstone1.sasscapstone1.entity.WhiteList;
import com.capstone1.sasscapstone1.enums.ErrorCode;
import com.capstone1.sasscapstone1.exception.ApiException;
import com.capstone1.sasscapstone1.repository.Account.AccountRepository;
import com.capstone1.sasscapstone1.repository.BlackList.BlackListRepository;
import com.capstone1.sasscapstone1.repository.Role.RoleRepository;
import com.capstone1.sasscapstone1.repository.WhiteList.WhiteListRepository;
import com.capstone1.sasscapstone1.request.*;
import com.capstone1.sasscapstone1.service.JwtService.JwtService;
import com.capstone1.sasscapstone1.util.CookieUtils;
import com.capstone1.sasscapstone1.util.CreateApiResponse;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.persistence.EntityManager;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {
    private final AccountRepository accountRepository;
    private final WhiteListRepository whiteListRepository;
    private final AuthenticationManager authenticationManager;
    private final BlackListRepository blackListRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final CookieUtils cookieUtils;
    private final RoleRepository roleRepository;
    private final UserDetailsService userDetailsService;
    private final EntityManager entityManager;

    private boolean checkExistEmail(String email){
        Optional<Account> existEmail= accountRepository.findAccountByEmail(email);
        return existEmail.isPresent();
    }
    @Override
    public ApiResponse<LoginDto> login(LoginRequest loginRequest, HttpServletResponse response) throws Exception {
        try{
            Authentication authentication= authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getEmail().toLowerCase(),loginRequest.getPassword()));
            if(!(authentication.getPrincipal() instanceof AnonymousAuthenticationToken)){
                Account account= (Account) authentication.getPrincipal();
                String accessToken= jwtService.GenerateAccessToken(account);
                String refreshToken= jwtService.GenerateRefreshToken(account);
                cookieUtils.generatorTokenCookie(response,accessToken,refreshToken);
                LocalDateTime now= LocalDateTime.now();
                LocalDateTime expiresRefreshToken= now.plusDays(2);
                Optional<WhiteList> findAccount= whiteListRepository.findByAccountId(account.getAccountId());
                findAccount.ifPresent(whiteListRepository::delete);
                WhiteList whiteListSave= WhiteList.builder()
                        .token(refreshToken)
                        .accountId(account.getAccountId())
                        .expirationToken(expiresRefreshToken)
                        .build();
                whiteListRepository.save(whiteListSave);
                Set<String> roles = account.getRoles().stream().map(Role::getName).collect(Collectors.toSet());
                AccountStatisticsDto getAccountStatisticsDto=null;
                if(!roles.contains("ROLE_ADMIN")){
                    getAccountStatisticsDto= accountRepository.getAccountStatistics(account.getAccountId());
                }
                return CreateApiResponse.createResponse(LoginDto.builder()
                        .accountId(account.getAccountId())
                        .accessToken(accessToken)
                        .refreshToken(refreshToken)
                        .listRoles(roles)
                        .username(account.getFirstName()+" "+account.getLastName())
                        .accountId(account.getAccountId())
                        .profilePicture(account.getProfilePicture())
                        .follower(getAccountStatisticsDto != null ? Math.toIntExact(getAccountStatisticsDto.getTotalFollowers()) : 0)
                        .following(getAccountStatisticsDto != null ? Math.toIntExact(getAccountStatisticsDto.getTotalFollowing()) : 0)
                        .upload(getAccountStatisticsDto != null ? Math.toIntExact(getAccountStatisticsDto.getTotalUploadedDocuments()) : 0)
                        .build()
                        ,false);
            }else{
                throw new ApiException(ErrorCode.BAD_REQUEST.getStatusCode().value(),"Email or password incorrect");
            }
        }catch (BadCredentialsException e){
            throw new ApiException(ErrorCode.BAD_REQUEST.getStatusCode().value(),"Email or password incorrect");
        }catch (ApiException e){
            throw new ApiException(e.getCode(),e.getMessage());
        }
        catch (Exception e){
            throw new Exception(e.getMessage());
        }
    }

    @Override
    public ApiResponse<LoginDto> autoLogin(HttpServletRequest request) throws Exception {
        try{
            Cookie[] cookies= request.getCookies();
            String accessToken = null;
            String refreshToken= null;
            if(cookies==null){
                throw new ApiException(ErrorCode.FORBIDDEN.getStatusCode().value(),"Token isn't valid");
            }
            for(Cookie cookie : cookies){
                if (cookie.getName().equals("accessToken")) {
                    accessToken = cookie.getValue();
                }else if(cookie.getName().equals("refreshToken")){
                    refreshToken= cookie.getValue();
                }
            }
            if (accessToken==null){
                throw new ApiException(ErrorCode.FORBIDDEN.getStatusCode().value(),"Access token is expires");
            }
            Authentication authentication= SecurityContextHolder.getContext().getAuthentication();
            if(!(authentication instanceof AnonymousAuthenticationToken)){
                Account loadUser= (Account) authentication.getPrincipal();
                Set<String> roles = loadUser.getRoles().stream().map(Role::getName).collect(Collectors.toSet());
                AccountStatisticsDto getAccountStatisticsDto=null;
                if(!roles.contains("ROLE_ADMIN")){
                    getAccountStatisticsDto= accountRepository.getAccountStatistics(loadUser.getAccountId());
                }
                return CreateApiResponse.createResponse(LoginDto.builder()
                        .accountId(loadUser.getAccountId())
                        .accessToken(accessToken)
                        .refreshToken(refreshToken)
                        .listRoles(loadUser.getRoles().stream().map(Role::getName).collect(Collectors.toSet()))
                        .username(loadUser.getFirstName()+" "+loadUser.getLastName())
                        .accountId(loadUser.getAccountId())
                        .profilePicture(loadUser.getProfilePicture())
                        .follower(getAccountStatisticsDto != null ? Math.toIntExact(getAccountStatisticsDto.getTotalFollowers()) : 0)
                        .following(getAccountStatisticsDto != null ? Math.toIntExact(getAccountStatisticsDto.getTotalFollowing()) : 0)
                        .upload(getAccountStatisticsDto != null ? Math.toIntExact(getAccountStatisticsDto.getTotalUploadedDocuments()) : 0)
                        .build(),false);
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
        String refreshToken = null;
        try{
            if(cookies==null){
                throw new ApiException(ErrorCode.UNAUTHORIZED.getStatusCode().value(),"refresh token is not found");
            }
            for(Cookie cookie : cookies){
                if (cookie.getName().equals("refreshToken")){
                    refreshToken=cookie.getValue();
                    break;
                }
            }
            if(refreshToken == null){
                throw new ApiException(ErrorCode.UNAUTHORIZED.getStatusCode().value(),"refresh token is not found");
            }
            Optional<BlackList> checkTokenInBlackList= blackListRepository.findBlackListsByToken(refreshToken);
            if (checkTokenInBlackList.isPresent()){
                throw new ApiException(ErrorCode.UNAUTHORIZED.getStatusCode().value(),"Token is disable");
            }
            String userName= jwtService.ExtractUsername(refreshToken);
            if(userName != null){
                Optional<Account> findAccount= accountRepository.findAccountByEmail(userName.toLowerCase());
                if(findAccount.isPresent()){
                    Account account= findAccount.get();
                    Optional<WhiteList> findToken= whiteListRepository.findByAccountId(account.getAccountId());
                    if(findToken.isPresent()){
                        if(findToken.get().getExpirationToken().isAfter(LocalDateTime.now())){
                            String newAccessToken= jwtService.GenerateAccessToken(account);
                            cookieUtils.generatorTokenCookie(response,newAccessToken,refreshToken);
                            return CreateApiResponse.createResponse("Refresh token success",false);
                        }else{
                            BlackList blackListSave= BlackList.builder()
                                    .token(findToken.get().getToken())
                                    .build();
                            blackListRepository.save(blackListSave);
                            whiteListRepository.delete(findToken.get());
                            throw new ApiException(ErrorCode.UNAUTHORIZED.getStatusCode().value(), "Refresh token is expires");
                        }
                    }else{
                        throw new ApiException(ErrorCode.BAD_REQUEST.getStatusCode().value(), "Refresh token not valid");
                    }
                }else{
                    throw new ApiException(ErrorCode.BAD_REQUEST.getStatusCode().value(), "Refresh token not valid");
                }
            }
            throw new ApiException(ErrorCode.UNAUTHORIZED.getStatusCode().value(), "Refresh token is expires");
        }catch (ApiException e){
            throw new ApiException(e.getCode(), e.getMessage());
        }catch (ExpiredJwtException e){
            cookieUtils.generatorTokenCookie(response,null,null);
            BlackList blackListSave= BlackList.builder()
                    .token(refreshToken)
                    .build();
            blackListRepository.save(blackListSave);
            Optional<WhiteList> findToken= whiteListRepository.findWhiteListByToken(refreshToken);
            findToken.ifPresent(whiteListRepository::delete);
            throw new ApiException(ErrorCode.UNAUTHORIZED.getStatusCode().value(), "Refresh token is expires");
        }catch (Exception e){
            throw new Exception(e.getMessage());
        }
    }

    @Override
    public ApiResponse<String> logout(HttpServletRequest request, HttpServletResponse response) throws Exception {
        try{
            Cookie[] cookies= request.getCookies();
            String refreshToken= null;
            if(cookies ==null){
                return CreateApiResponse.createResponse("Logout success",false);
            }
            for(Cookie cookie : cookies){
                if (cookie.getName().equals("refreshToken")){
                    refreshToken= cookie.getValue();
                    break;
                }
            }
            if(refreshToken == null){
                return CreateApiResponse.createResponse("Logout success",false);
            }
            Optional<WhiteList> isToken= whiteListRepository.findWhiteListByToken(refreshToken);
            isToken.ifPresent(whiteListRepository::delete);
            BlackList blackListSave= BlackList.builder()
                    .token(refreshToken)
                    .build();
            blackListRepository.save(blackListSave);
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
            Account account= (Account) userDetailsService.loadUserByUsername(request.getEmail().toLowerCase());
            if(request.getOtp() != null){
                WhiteList whiteList= WhiteList.builder()
                        .token(request.getOtp())
                        .accountId(account.getAccountId())
                        .expirationToken(LocalDateTime.now().plusMinutes(5))
                        .build();
                whiteListRepository.save(whiteList);
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
            Optional<WhiteList> findToken= whiteListRepository.findWhiteListByToken(request.getOtp());
            if(findToken.isPresent() && findToken.get().getExpirationToken().isAfter(LocalDateTime.now())){
                Account findAccount = accountRepository.findAccountByEmail(request.getEmail().toLowerCase()).orElseThrow(()->new ApiException(ErrorCode.BAD_REQUEST.getStatusCode().value(),"Account not found"));
                findAccount.setPassword(passwordEncoder.encode(request.getPassword()));
                accountRepository.save(findAccount);
                BlackList blackList= BlackList.builder().token(findToken.get().getToken()).build();
                blackListRepository.save(blackList);
                whiteListRepository.delete(findToken.get());
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
            Optional<WhiteList> findToken= whiteListRepository.findWhiteListByToken(request.getOtp());
            if(findToken.isPresent()){
                BlackList blackList= BlackList.builder().token(findToken.get().getToken()).build();
                blackListRepository.save(blackList);
                whiteListRepository.delete(findToken.get());
                return CreateApiResponse.createResponse("Clear token is success",false);
            }else{
                throw new ApiException(ErrorCode.BAD_REQUEST.getStatusCode().value(),"token isn't valid");
            }
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
