package org.com.identityservice.controller.Web;

import lombok.RequiredArgsConstructor;
import org.com.identityservice.dto.request.UpdateUserProfileRequest;
import org.com.identityservice.dto.response.AccountDto;
import org.com.identityservice.dto.response.ApiResponse;
import org.com.identityservice.dto.response.SearchUserResponseDto;
import org.com.identityservice.dto.response.UserProfileResponse;
import org.com.identityservice.entity.Account;
import org.com.identityservice.enums.ErrorCode;
import org.com.identityservice.exception.ApiException;
import org.com.identityservice.helpers.CreateApiResponse;
import org.com.identityservice.service.AccountService;
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
            @RequestPart(value = "profilePicture", required = false) MultipartFile profilePicture) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            Account account = (Account) authentication.getPrincipal();
            try {
                return accountService.updateUserProfile(account, request, profilePicture);
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
            Account account = (Account) authentication.getPrincipal();
            Long accountId = account.getAccountId();

            accountService.deleteProfilePicture(accountId);

            return CreateApiResponse.createResponse("Profile picture deleted successfully.",false);
        } else {
            throw new ApiException(ErrorCode.FORBIDDEN.getStatusCode().value(),"You are not authorized to perform this action.");
        }
    }
}
