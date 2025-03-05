package com.capstone1.sasscapstone1.service.FollowService;

import com.capstone1.sasscapstone1.dto.AccountDto.AccountDto;
import com.capstone1.sasscapstone1.dto.FollowDto.FollowDto;
import com.capstone1.sasscapstone1.dto.response.ApiResponse;

import java.util.List;

public interface FollowService {
    ApiResponse<String> followUserByEmail(String email, AccountDto account) throws Exception;

    ApiResponse<String> unfollowUserByEmail(String email, AccountDto account) throws Exception;

    ApiResponse<List<FollowDto>> getFollowers(AccountDto currentUser) throws Exception;

    ApiResponse<List<FollowDto>> getFollowing(AccountDto currentUser) throws Exception;
}
