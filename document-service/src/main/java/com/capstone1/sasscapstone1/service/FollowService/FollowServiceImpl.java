package com.capstone1.sasscapstone1.service.FollowService;

import com.capstone1.sasscapstone1.dto.FollowDto.FollowDto;
import com.capstone1.sasscapstone1.dto.response.ApiResponse;
import com.capstone1.sasscapstone1.entity.Account;
import com.capstone1.sasscapstone1.entity.Follow;
import com.capstone1.sasscapstone1.enums.ErrorCode;
import com.capstone1.sasscapstone1.exception.ApiException;
import com.capstone1.sasscapstone1.repository.Account.AccountRepository;
import com.capstone1.sasscapstone1.repository.Follow.FollowRepository;
import com.capstone1.sasscapstone1.service.KafkaService.KafkaService;
import com.capstone1.sasscapstone1.util.CreateApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class FollowServiceImpl implements FollowService {

    @Autowired
    private FollowRepository followRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private KafkaService kafkaService;

    @Override
    public ApiResponse<String> followUserByEmail(String email, Account account) throws Exception {
        try {
            // Kiểm tra người dùng cần follow có tồn tại không
            Account accountToFollow = accountRepository.findAccountByEmail(email)
                    .orElseThrow(() -> new ApiException(ErrorCode.BAD_REQUEST.getStatusCode().value(),"User with email " + email + " not found"));

            // Kiểm tra người dùng có thể không tự follow chính mình
            if (Long.valueOf(account.getAccountId()).equals(accountToFollow.getAccountId())) {
                throw new ApiException(ErrorCode.BAD_REQUEST.getStatusCode().value(),"You cannot follow yourself.");
            }

            // Tạo bản ghi follow
            Follow follow = new Follow();
            follow.setFollower(account);
            follow.setFollowing(accountToFollow);

            followRepository.save(follow);

            kafkaService.sendNotificationFromUserFollowing(accountToFollow, account.getLastName());

            return CreateApiResponse.createResponse("You are now following " + accountToFollow.getEmail(),false);
        } catch (ApiException e) {
            throw new ApiException(e.getCode(),e.getMessage());
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    @Override
    @Transactional
    public ApiResponse<String> unfollowUserByEmail(String email, Account account) throws Exception {
        try {
            // Kiểm tra người dùng cần unfollow có tồn tại không
            Account accountToUnfollow = accountRepository.findAccountByEmail(email)
                    .orElseThrow(() -> new ApiException(ErrorCode.BAD_REQUEST.getStatusCode().value(),"User with email " + email + " not found"));

            // Kiểm tra người dùng có thể không tự unfollow chính mình
            if (Long.valueOf(account.getAccountId()).equals(accountToUnfollow.getAccountId())) {
                throw new ApiException(ErrorCode.BAD_REQUEST.getStatusCode().value(),"You cannot unfollow yourself.");
            }

            // Xóa bản ghi follow
            Follow follow = followRepository.findByFollowerAndFollowing(account, accountToUnfollow)
                    .orElseThrow(() -> new ApiException(ErrorCode.BAD_REQUEST.getStatusCode().value(),"Follow relationship not found"));

            followRepository.delete(follow);

            return CreateApiResponse.createResponse("You have unfollowed " + accountToUnfollow.getEmail(),false);
        } catch (ApiException e) {
            throw new ApiException(e.getCode(),e.getMessage());
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    @Override
    public ApiResponse<List<FollowDto>> getFollowers(Account account) throws Exception {
        try {
            // Lấy danh sách các followers của người dùng
            List<Follow> followers = followRepository.findByFollowing(account);
            if (followers.isEmpty()) {
                throw new ApiException(ErrorCode.BAD_REQUEST.getStatusCode().value(),"No followers found for this user.");
            }

            // Ánh xạ danh sách Follow sang FollowDto
            List<FollowDto> followerDtos = followers.stream().map(follow -> {
                Account followerAccount = follow.getFollower();
                FollowDto dto = new FollowDto();
                dto.setFollowId(follow.getFollowId());
                dto.setFirstName(followerAccount.getFirstName());
                dto.setLastName(followerAccount.getLastName());
                dto.setProfilePicture(followerAccount.getProfilePicture());
                return dto;
            }).toList();

            return CreateApiResponse.createResponse(followerDtos,false);
        } catch (ApiException e) {
            throw new ApiException(e.getCode(),e.getMessage());
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    @Override
    public ApiResponse<List<FollowDto>> getFollowing(Account account) throws Exception {
        try {
            List<Follow> followings = followRepository.findByFollower(account);
            if (followings.isEmpty()) {
                throw new ApiException(ErrorCode.BAD_REQUEST.getStatusCode().value(),"You are not following anyone.");
            }

            // Map Follow entity to FollowDto
            List<FollowDto> followingDtos = followings.stream().map(follow -> {
                Account followingAccount = follow.getFollowing();
                FollowDto dto = new FollowDto();
                dto.setFollowId(follow.getFollowId());
                dto.setFirstName(followingAccount.getFirstName());
                dto.setLastName(followingAccount.getLastName());
                dto.setProfilePicture(followingAccount.getProfilePicture());
                return dto;
            }).toList();

            return CreateApiResponse.createResponse(followingDtos,false);
        } catch (ApiException e) {
            throw new ApiException(e.getCode(),e.getMessage());
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }
}
