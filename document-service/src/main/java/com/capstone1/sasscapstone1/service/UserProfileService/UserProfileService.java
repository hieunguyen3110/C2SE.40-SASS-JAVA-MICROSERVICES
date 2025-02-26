package com.capstone1.sasscapstone1.service.UserProfileService;

import com.capstone1.sasscapstone1.dto.UserProfileResponseDTO.UserProfileResponse;
import com.capstone1.sasscapstone1.entity.Account;
import org.springframework.http.ResponseEntity;

public interface UserProfileService {
    UserProfileResponse getUserProfile(String email) throws Exception;
    UserProfileResponse getUserProfile(String email, Account account) throws Exception;
}
