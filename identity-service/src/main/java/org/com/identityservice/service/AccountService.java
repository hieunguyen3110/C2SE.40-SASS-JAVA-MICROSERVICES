package org.com.identityservice.service;

import org.com.identityservice.dto.request.UpdateUserProfileRequest;
import org.com.identityservice.dto.response.*;
import org.com.identityservice.entity.Account;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface AccountService {
    ApiResponse<String> allowActiveAccount(String email) throws Exception;
    Page<AccountDto> listUsers(int page, int size);
    List<AccountDto> getAllNewUserByDay() throws Exception;
    List<AccountDto> getAllNewUserByDayIsActiveIsTrue() throws Exception;
    List<AccountDto> getAllAccountIsAnalyze() throws Exception;
    AccountDto getUserDetails(Long accountId);
    AccountDto getUserDetails(String email);
    Long countStatsByRoleName(String roleName);
    void softDeleteAccounts(List<Long> accountIds);
//    ApiResponse<List<AccountRatingDto>> getAccountByAccountIds(List<Long> accountIds, String docTitle, long docId) throws Exception;
    ApiResponse<String> updateListAccountRatingAtRedis(List<Long> accountIds, String docTitle, long docId, long rating) throws Exception;
    ApiResponse<String> approveNewUsers(List<Long> accountIds);
    String approveNewUsers(Long accountId);
    ApiResponse<String>adminDeleteUserProfilePicture(Long accountId);
    ApiResponse<List<SearchUserResponseDto>> searchUsersByName(String name, Long accountId, int pageNum, int pageSize);
    ApiResponse<UserProfileResponse> updateUserProfile(AccountDto account, UpdateUserProfileRequest request, MultipartFile profilePicture);
    void deleteProfilePicture(Long accountId);

    String enableStudyAnalyze(AccountDto accountDto) throws Exception;
    String disableStudyAnalyze(AccountDto accountDto) throws Exception;
}
