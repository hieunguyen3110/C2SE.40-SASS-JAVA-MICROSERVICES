package com.capstone1.sasscapstone1.service.UserProfileService;

import com.capstone1.sasscapstone1.dto.AccountDto.AccountDto;
import com.capstone1.sasscapstone1.dto.UserProfileResponseDTO.UserProfileResponse;

public interface UserProfileService {
    UserProfileResponse getUserProfile(String email) throws Exception;
    UserProfileResponse getUserProfile(String email, AccountDto account) throws Exception;
}
