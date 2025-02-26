package com.capstone1.sasscapstone1.service.UserUpdateProfileService;

import com.capstone1.sasscapstone1.dto.UpdateUserProfileRequestDto.UpdateUserProfileRequest;
import com.capstone1.sasscapstone1.dto.UserProfileResponseDTO.UserProfileResponse;
import com.capstone1.sasscapstone1.dto.response.ApiResponse;
import com.capstone1.sasscapstone1.entity.Account;
import org.springframework.web.multipart.MultipartFile;

public interface UserUpdateProfileService {
    ApiResponse<UserProfileResponse> updateUserProfile(Account account, UpdateUserProfileRequest request, MultipartFile profilePicture);
    void deleteProfilePicture(Long accountId);
}
