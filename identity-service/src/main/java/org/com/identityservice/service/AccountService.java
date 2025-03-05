package org.com.identityservice.service;

import org.com.identityservice.dto.request.UpdateUserProfileRequest;
import org.com.identityservice.dto.response.AccountDto;
import org.com.identityservice.dto.response.ApiResponse;
import org.com.identityservice.dto.response.SearchUserResponseDto;
import org.com.identityservice.dto.response.UserProfileResponse;
import org.com.identityservice.entity.Account;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface AccountService {
    ApiResponse<String> allowActiveAccount(String email) throws Exception;
    Page<AccountDto> listUsers(int page, int size);
    AccountDto getUserDetails(Long accountId);
    AccountDto getUserDetails(String email);
    Long countStatsByRoleName(String roleName);
    void softDeleteAccounts(List<Long> accountIds);
    ApiResponse<String> approveNewUsers(List<Long> accountIds);
    ApiResponse<String>adminDeleteUserProfilePicture(Long accountId);
    ApiResponse<List<SearchUserResponseDto>> searchUsersByName(String name, Long accountId, int pageNum, int pageSize);
    ApiResponse<UserProfileResponse> updateUserProfile(Account account, UpdateUserProfileRequest request, MultipartFile profilePicture);
    void deleteProfilePicture(Long accountId);
}
