package com.capstone1.sasscapstone1.service.FollowService;

import com.capstone1.sasscapstone1.dto.FollowDto.FollowDto;
import com.capstone1.sasscapstone1.dto.response.ApiResponse;
import com.capstone1.sasscapstone1.entity.Account;

import java.util.List;
import java.util.Optional;

public interface FollowService {
    ApiResponse<String> followUserByEmail(String email, Account account) throws Exception;

    ApiResponse<String> unfollowUserByEmail(String email, Account account) throws Exception;

    ApiResponse<List<FollowDto>> getFollowers(Account currentUser) throws Exception;

    ApiResponse<List<FollowDto>> getFollowing(Account currentUser) throws Exception;
}
