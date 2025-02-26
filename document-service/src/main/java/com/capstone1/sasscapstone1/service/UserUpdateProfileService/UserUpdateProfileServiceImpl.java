package com.capstone1.sasscapstone1.service.UserUpdateProfileService;

import com.capstone1.sasscapstone1.dto.UpdateUserProfileRequestDto.UpdateUserProfileRequest;
import com.capstone1.sasscapstone1.dto.UserProfileResponseDTO.UserProfileResponse;
import com.capstone1.sasscapstone1.dto.response.ApiResponse;
import com.capstone1.sasscapstone1.entity.Account;
import com.capstone1.sasscapstone1.enums.ErrorCode;
import com.capstone1.sasscapstone1.exception.ApiException;
import com.capstone1.sasscapstone1.repository.Account.AccountRepository;
import com.capstone1.sasscapstone1.service.FirebaseService.FirebaseService;
import com.capstone1.sasscapstone1.util.CreateApiResponse;
import com.capstone1.sasscapstone1.util.UserProfileUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Service
@RequiredArgsConstructor
public class UserUpdateProfileServiceImpl implements UserUpdateProfileService {

    private final AccountRepository accountRepository;
    private final FirebaseService firebaseService;
    private final UserProfileUtils userProfileUtils;

    @Override
    @Transactional
    public ApiResponse<UserProfileResponse> updateUserProfile(Account account, UpdateUserProfileRequest request, MultipartFile profilePicture) {
        try {
            // Cập nhật thông tin cá nhân
            userProfileUtils.updatePersonalInfo(account, request);

            // Xử lý ảnh đại diện nếu có
            if (profilePicture != null && !profilePicture.isEmpty()) {
                // Xóa ảnh đại diện cũ nếu có
                String oldProfilePictureUrl = account.getProfilePicture();
                if (oldProfilePictureUrl != null && !oldProfilePictureUrl.isEmpty()) {
                    deleteProfilePicture(account.getAccountId());
                }

                // Tải ảnh đại diện mới lên
                String profilePictureUrl = uploadProfilePicture(profilePicture);
                account.setProfilePicture(profilePictureUrl);
            }

            // Lưu tài khoản sau khi cập nhật
            Account updatedAccount = accountRepository.save(account);
            return CreateApiResponse.createResponse(userProfileUtils.mapToUserProfileResponse(updatedAccount, null, null),false);
        } catch (ApiException e) {
            throw new ApiException(e.getCode(),"Update failed: " + e.getMessage());
        } catch (RuntimeException e) {
            throw new RuntimeException("Unexpected error during profile update: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException("General error during profile update: " + e.getMessage(), e);
        }
    }

    private String uploadProfilePicture(MultipartFile profilePicture) {
        try {
            String originalFileName = profilePicture.getOriginalFilename();
            if (originalFileName == null || originalFileName.isBlank()) {
                throw new ApiException(ErrorCode.BAD_REQUEST.getStatusCode().value(),"Invalid file name.");
            }

            BufferedImage bufferedImage = ImageIO.read(profilePicture.getInputStream());
            CompletableFuture<String> uploadFuture = firebaseService.save(bufferedImage, originalFileName);
            String profilePictureUrl = uploadFuture.get();
            System.out.println("Profile Picture URL: " + profilePictureUrl);
            return profilePictureUrl;

        } catch (IOException e) {
            throw new ApiException(ErrorCode.BAD_GATEWAY.getStatusCode().value(),"Error processing profile picture: " + e.getMessage());

        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            throw new ApiException(ErrorCode.BAD_GATEWAY.getStatusCode().value(),"Error uploading profile picture: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public void deleteProfilePicture(Long accountId) {
        try {
            // Tìm tài khoản
            Account account = accountRepository.findById(accountId)
                    .orElseThrow(() -> new ApiException(ErrorCode.BAD_REQUEST.getStatusCode().value(),"User not found with ID: " + accountId));

            // Kiểm tra xem có ảnh đại diện không
            String profilePictureUrl = account.getProfilePicture();
            if (profilePictureUrl == null || profilePictureUrl.isBlank()) {
                throw new ApiException(ErrorCode.BAD_REQUEST.getStatusCode().value(),"No profile picture to delete.");
            }

            // Xóa ảnh trên Firebase
            firebaseService.delete(profilePictureUrl);

            // Xóa đường dẫn ảnh trong tài khoản
            account.setProfilePicture(null);
            accountRepository.save(account);

        } catch (ApiException e) {
            throw new ApiException(e.getCode(),"Error deleting profile picture: " + e.getMessage());

        } catch (Exception e) {
            throw new RuntimeException("Unexpected error while deleting profile picture: " + e.getMessage(), e);
        }
    }
}
