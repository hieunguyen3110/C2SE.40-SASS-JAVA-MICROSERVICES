package com.capstone1.sasscapstone1.service.FollowService;

import com.capstone1.sasscapstone1.dto.AccountDto.AccountDto;
import com.capstone1.sasscapstone1.dto.FollowDto.FollowDto;
import com.capstone1.sasscapstone1.dto.response.ApiResponse;
import com.capstone1.sasscapstone1.entity.Follow;
import com.capstone1.sasscapstone1.enums.ErrorCode;
import com.capstone1.sasscapstone1.exception.ApiException;
import com.capstone1.sasscapstone1.producer.NotificationProducer;
import com.capstone1.sasscapstone1.repository.Follow.FollowRepository;
import com.capstone1.sasscapstone1.repository.httpClient.IdentityClient;
import com.capstone1.sasscapstone1.util.CreateApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FollowServiceImpl implements FollowService {
    private final FollowRepository followRepository;
    private final IdentityClient identityClient;
    private final NotificationProducer notificationProducer;

    @Override
    public ApiResponse<String> followUserByEmail(String email, AccountDto account) throws Exception {
        try {
            // Kiểm tra người dùng cần follow có tồn tại không
            AccountDto accountToFollow = identityClient.getAccountEmail(email).getData();
            // Kiểm tra người dùng có thể không tự follow chính mình
            if (Long.valueOf(account.getAccountId()).equals(accountToFollow.getAccountId())) {
                throw new ApiException(ErrorCode.BAD_REQUEST.getStatusCode().value(),"You cannot follow yourself.");
            }

            // Tạo bản ghi follow
            Follow follow = new Follow();
            follow.setFollowerId(account.getAccountId());
            follow.setFollowingId(accountToFollow.getAccountId());

            followRepository.save(follow);

            // send notification with kafka
            notificationProducer.sendNotificationFromUserFollowing(accountToFollow, account.getLastName());

            return CreateApiResponse.createResponse("You are now following " + accountToFollow.getEmail(),false);
        } catch (ApiException e) {
            throw new ApiException(e.getCode(),e.getMessage());
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    @Override
    @Transactional
    public ApiResponse<String> unfollowUserByEmail(String email, AccountDto account) throws Exception {
        try {
            // Kiểm tra người dùng cần unfollow có tồn tại không
            AccountDto accountToUnfollow = identityClient.getAccountEmail(email).getData();
            // Kiểm tra người dùng có thể không tự unfollow chính mình
            if (Long.valueOf(account.getAccountId()).equals(accountToUnfollow.getAccountId())) {
                throw new ApiException(ErrorCode.BAD_REQUEST.getStatusCode().value(),"You cannot unfollow yourself.");
            }

            // Xóa bản ghi follow
            Follow follow = followRepository.findByFollowerIdAndFollowingId(account.getAccountId(), accountToUnfollow.getAccountId())
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
    public ApiResponse<List<FollowDto>> getFollowers(AccountDto account) throws Exception {
        try {
            // Lấy danh sách các followers của người dùng
            List<Follow> followers = followRepository.findByFollowingId(account.getAccountId());
            if (followers.isEmpty()) {
                throw new ApiException(ErrorCode.BAD_REQUEST.getStatusCode().value(),"No followers found for this user.");
            }

            // Ánh xạ danh sách Follow sang FollowDto
            List<FollowDto> followerDtos = followers.stream().map(follow -> {
                long followerAccountId = follow.getFollowerId();
                AccountDto accountDto= identityClient.getAccountId(followerAccountId).getData();
                FollowDto dto = new FollowDto();
                dto.setFollowId(follow.getFollowId());
                dto.setFirstName(accountDto.getFirstName());
                dto.setLastName(accountDto.getLastName());
                dto.setProfilePicture(accountDto.getProfilePicture());
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
    public ApiResponse<List<FollowDto>> getFollowing(AccountDto account) throws Exception {
        try {
            List<Follow> followings = followRepository.findByFollowerId(account.getAccountId());
            if (followings.isEmpty()) {
                throw new ApiException(ErrorCode.BAD_REQUEST.getStatusCode().value(),"You are not following anyone.");
            }

            // Map Follow entity to FollowDto
            List<FollowDto> followingDtos = followings.stream().map(follow -> {
                long followingAccount = follow.getFollowingId();
                FollowDto dto = new FollowDto();
                AccountDto accountDto= identityClient.getAccountId(followingAccount).getData();
                dto.setFollowId(follow.getFollowId());
                dto.setFirstName(accountDto.getFirstName());
                dto.setLastName(accountDto.getLastName());
                dto.setProfilePicture(accountDto.getProfilePicture());
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
