package org.com.identityservice.controller.Web;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.com.identityservice.dto.request.UpdateUserProfileRequest;
import org.com.identityservice.dto.response.*;
import org.com.identityservice.entity.Account;
import org.com.identityservice.enums.ErrorCode;
import org.com.identityservice.exception.ApiException;
import org.com.identityservice.helpers.CreateApiResponse;
import org.com.identityservice.producer.UserUpdateProducer;
import org.com.identityservice.service.AccountService;
import org.com.identityservice.service.RedisService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController("webAccountController")
@RequestMapping("/account")
@RequiredArgsConstructor
public class AccountController {
    private final AccountService accountService;
    private final RedisService redisService;
    private final ObjectMapper objectMapper;
    private final UserUpdateProducer userUpdateProducer;

    private void updateAccountDto(AccountDto accountDto,UserProfileResponse userProfileResponse){
        accountDto.setFirstName(userProfileResponse.getFirstName());
        accountDto.setLastName(userProfileResponse.getLastName());
        accountDto.setEmail(userProfileResponse.getEmail());
        accountDto.setProfilePicture(userProfileResponse.getProfilePicture());
        accountDto.setBirthDate(userProfileResponse.getBirthDate());
        accountDto.setGender(userProfileResponse.getGender());
        accountDto.setHometown(userProfileResponse.getHometown());
        accountDto.setPhoneNumber(userProfileResponse.getPhoneNumber());
        accountDto.setMajor(userProfileResponse.getMajor());
        accountDto.setEnrollmentYear(userProfileResponse.getEnrollmentYear());
        accountDto.setClassNumber(userProfileResponse.getClassNumber());
    }

    @GetMapping("/{accountId}")
    public ApiResponse<AccountDto> getUserDetails(@PathVariable Long accountId) {
        return CreateApiResponse.createResponse(accountService.getUserDetails(accountId),false);
    }
    @GetMapping("")
    public ApiResponse<AccountDto> getUserDetailsByEmail(@RequestParam("email") String email) {
        return CreateApiResponse.createResponse(accountService.getUserDetails(email),false);
    }
    @GetMapping("/search-by-name")
    public ApiResponse<List<SearchUserResponseDto>> searchUsersByName(@RequestParam String name,
                                                                      @RequestParam(defaultValue = "0") int pageNum,
                                                                      @RequestParam(defaultValue = "5") int pageSize) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            AccountDto loggedInAccount = (AccountDto) authentication.getPrincipal();

            return accountService.searchUsersByName(name, loggedInAccount.getAccountId(),pageNum,pageSize);
        } else {
            throw new ApiException(ErrorCode.FORBIDDEN.getStatusCode().value(),"You are not authorized to perform this action.");
        }
    }

    @PutMapping("/update-profile")
    public ApiResponse<UserProfileResponse> updateUserProfile(
            @ModelAttribute UpdateUserProfileRequest request,
            @RequestPart(value = "profilePicture", required = false) MultipartFile profilePicture, HttpServletRequest httpServletRequest)
    {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            AccountDto account = (AccountDto) authentication.getPrincipal();
            try {
                ApiResponse<UserProfileResponse> response= accountService.updateUserProfile(account, request, profilePicture);
                String authHeader= httpServletRequest.getHeader("Authorization");
                if(authHeader != null){
                    String token= authHeader.substring(7);
                    String json= (String) redisService.getData(token);
                    AccountDto extractAccountFromRedis= objectMapper.readValue(json,AccountDto.class);
                    updateAccountDto(extractAccountFromRedis,response.getData());
                    userUpdateProducer.sendEventUpdateUser(token,extractAccountFromRedis);
                }
                return response;
            } catch (Exception e) {
                throw new ApiException(ErrorCode.INTERNAL_SERVER_ERROR.getStatusCode().value(),"Error updating profile: " + e.getMessage());
            }
        } else {
            throw new ApiException(ErrorCode.FORBIDDEN.getStatusCode().value(),"You are not authorized to perform this action.");
        }
    }

    @DeleteMapping("/delete-profile-picture")
    public ApiResponse<String> deleteProfilePicture() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            AccountDto account = (AccountDto) authentication.getPrincipal();
            Long accountId = account.getAccountId();

            accountService.deleteProfilePicture(accountId);

            return CreateApiResponse.createResponse("Profile picture deleted successfully.",false);
        } else {
            throw new ApiException(ErrorCode.FORBIDDEN.getStatusCode().value(),"You are not authorized to perform this action.");
        }
    }


}
