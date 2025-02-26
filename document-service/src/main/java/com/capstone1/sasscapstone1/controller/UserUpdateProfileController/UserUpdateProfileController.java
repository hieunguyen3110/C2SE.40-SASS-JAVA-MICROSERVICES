package com.capstone1.sasscapstone1.controller.UserUpdateProfileController;

import com.capstone1.sasscapstone1.dto.UserProfileResponseDTO.UserProfileResponse;
import com.capstone1.sasscapstone1.dto.response.ApiResponse;
import com.capstone1.sasscapstone1.entity.Account;
import com.capstone1.sasscapstone1.dto.UpdateUserProfileRequestDto.UpdateUserProfileRequest;
import com.capstone1.sasscapstone1.enums.ErrorCode;
import com.capstone1.sasscapstone1.exception.ApiException;
import com.capstone1.sasscapstone1.service.UserUpdateProfileService.UserUpdateProfileService;
import com.capstone1.sasscapstone1.util.CreateApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/user")
public class UserUpdateProfileController {

    private final UserUpdateProfileService userProfileService;

    @Autowired
    public UserUpdateProfileController(UserUpdateProfileService userProfileService) {
        this.userProfileService = userProfileService;
    }

    @PutMapping("update-profile")
    public ApiResponse<UserProfileResponse> updateUserProfile(
            @ModelAttribute UpdateUserProfileRequest request,
            @RequestPart(value = "profilePicture", required = false) MultipartFile profilePicture) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            Account account = (Account) authentication.getPrincipal();
            try {
                return userProfileService.updateUserProfile(account, request, profilePicture);
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

            userProfileService.deleteProfilePicture(accountId);

            return CreateApiResponse.createResponse("Profile picture deleted successfully.",false);
        } else {
            throw new ApiException(ErrorCode.FORBIDDEN.getStatusCode().value(),"You are not authorized to perform this action.");
        }
    }
}

