package com.capstone1.sasscapstone1.service.AdminUserManagementService;

import com.capstone1.sasscapstone1.dto.UpdateUserProfileRequestDto.UpdateUserProfileRequest;
import com.capstone1.sasscapstone1.dto.UserProfileResponseDTO.UserProfileResponse;
import com.capstone1.sasscapstone1.dto.UserListDto.UserListDto;
import com.capstone1.sasscapstone1.dto.response.ApiResponse;
import com.capstone1.sasscapstone1.entity.Account;
import com.capstone1.sasscapstone1.entity.Faculty;
import com.capstone1.sasscapstone1.entity.Role;
import com.capstone1.sasscapstone1.enums.ErrorCode;
import com.capstone1.sasscapstone1.exception.ApiException;
import com.capstone1.sasscapstone1.repository.Account.AccountRepository;
import com.capstone1.sasscapstone1.repository.AccountRole.AccountRoleRepository;
import com.capstone1.sasscapstone1.repository.Faculty.FacultyRepository;
import com.capstone1.sasscapstone1.service.FirebaseService.FirebaseService;
import com.capstone1.sasscapstone1.util.CreateApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Service
public class AdminUserManagementServiceImpl implements AdminUserManagementService {

    private final AccountRepository accountRepository;
    private final AccountRoleRepository accountRoleRepository;
    private final FacultyRepository facultyRepository;
    private final FirebaseService firebaseService;

    @Autowired
    public AdminUserManagementServiceImpl(AccountRepository accountRepository, AccountRoleRepository accountRoleRepository,
                                          FacultyRepository facultyRepository, FirebaseService firebaseService) {
        this.accountRepository = accountRepository;
        this.accountRoleRepository = accountRoleRepository;
        this.facultyRepository = facultyRepository;
        this.firebaseService = firebaseService;
    }

    @Override
    public Page<UserListDto> listUsers(int page, int size) {
        try {
            return accountRepository.findAll(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "updatedAt")))
                    .map(account -> {
                        UserListDto dto = new UserListDto();
                        dto.setAccountId(account.getAccountId());
                        dto.setFirstName(account.getFirstName());
                        dto.setLastName(account.getLastName());
                        dto.setEmail(account.getEmail());
                        dto.setBirthDate(account.getBirthDate() != null ? account.getBirthDate().toString() : "N/A");
                        dto.setGender(account.getGender());
                        dto.setHometown(account.getHometown());
                        dto.setIsActive(account.getIsActive());
                        dto.setRole(account.getRoles().stream()
                                .findFirst()
                                .map(Role::getName)
                                .orElse("N/A"));
                        return dto;
                    });
        } catch (Exception e) {
            throw new RuntimeException("Error listing users: " + e.getMessage(), e);
        }
    }

    @Override
    public UserProfileResponse getUserDetails(Long accountId) {
        try {
            Account account = accountRepository.findById(accountId)
                    .orElseThrow(() -> new ApiException(ErrorCode.BAD_REQUEST.getStatusCode().value(),"User not found with ID: " + accountId));

            UserProfileResponse dto = new UserProfileResponse();
            dto.setFirstName(account.getFirstName());
            dto.setLastName(account.getLastName());
            dto.setEmail(account.getEmail());
            dto.setBirthDate(account.getBirthDate());
            dto.setGender(account.getGender());
            dto.setHometown(account.getHometown());
            dto.setPhoneNumber(account.getPhoneNumber());
            dto.setMajor(account.getMajor());
            dto.setFacultyName(account.getFaculty() != null ? account.getFaculty().getFacultyName() : null);
            dto.setEnrollmentYear(account.getEnrollmentYear());
            dto.setClassNumber(account.getClassNumber());
            dto.setProfilePicture(account.getProfilePicture());

            List<String> roles = accountRoleRepository.findRoleNamesByAccountId(account.getAccountId());
            dto.setRoles(roles);

            return dto;
        } catch (ApiException e) {
            throw new ApiException(e.getCode(),"Error fetching user details: " + e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException("An unexpected error occurred while fetching user details: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public void softDeleteAccounts(List<Long> accountIds) {
        try {
            List<Account> accounts = accountRepository.findAllById(accountIds);
            accounts.forEach(account -> {
                account.setIsDeleted(true);
                account.setIsActive(false);
            });
            accountRepository.saveAll(accounts);
        } catch (Exception e) {
            throw new RuntimeException("Error performing soft delete: " + e.getMessage(), e);
        }
    }

    @Override
    public ApiResponse<String> approveNewUsers(List<Long> accountIds) {
        try {
            List<Account> accounts = accountRepository.findAllById(accountIds);
            accounts.forEach(account -> account.setIsActive(true));
            accountRepository.saveAll(accounts);
            return CreateApiResponse.createResponse("Users approved successfully.",false);
        } catch (Exception e) {
            throw new ApiException(ErrorCode.BAD_GATEWAY.getStatusCode().value(),"Error approving users: " + e.getMessage());
        }
    }

    private UserProfileResponse mapToUserProfileResponse(Account account){
        List<String> roles= new ArrayList<>();

        for(Role roleEntity: account.getRoles()){
            roles.add(roleEntity.getName());
        }
        UserProfileResponse response = new UserProfileResponse();
        response.setFirstName(account.getFirstName());
        response.setLastName(account.getLastName());
        response.setEmail(account.getEmail());
        response.setProfilePicture(account.getProfilePicture());
        response.setBirthDate(account.getBirthDate());
        response.setGender(account.getGender());
        response.setHometown(account.getHometown());
        response.setPhoneNumber(account.getPhoneNumber());
        response.setMajor(account.getMajor());
        response.setEnrollmentYear(account.getEnrollmentYear());
        response.setClassNumber(account.getClassNumber());
        response.setRoles(roles);
        if (account.getFaculty() != null) {
            response.setFacultyName(account.getFaculty().getFacultyName());
        }
        return response;
    }

    @Override
    @Transactional
    public ApiResponse<String> adminDeleteUserProfilePicture(Long accountId) {
        try {
            deleteProfilePicture(accountId);
            return CreateApiResponse.createResponse("Profile picture deleted successfully.",false);
        } catch (ApiException e) {
            throw new ApiException(e.getCode(),e.getMessage());
        } catch (RuntimeException e) {
            throw new RuntimeException("Unexpected error during profile update: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException("General error during profile update: " + e.getMessage(), e);
        }
    }

    // Hàm xóa ảnh đại diện cho quản trị viên
    @Transactional
    public void deleteProfilePicture(Long accountId) {
        try {

            Account account = accountRepository.findById(accountId)
                    .orElseThrow(() -> new ApiException(ErrorCode.BAD_REQUEST.getStatusCode().value(),"User not found with ID: " + accountId));


            String profilePictureUrl = account.getProfilePicture();
            if (profilePictureUrl == null || profilePictureUrl.isBlank()) {
                throw new ApiException(ErrorCode.BAD_REQUEST.getStatusCode().value(),"No profile picture to delete.");
            }

            firebaseService.delete(profilePictureUrl);

            account.setProfilePicture(null);
            accountRepository.save(account);

        } catch (ApiException e) {
            throw new ApiException(e.getCode(),"Error deleting profile picture: " + e.getMessage());

        } catch (Exception e) {
            throw new RuntimeException("Unexpected error while deleting profile picture: " + e.getMessage(), e);
        }
    }

    private void updatePersonalInfo(Account account, UpdateUserProfileRequest request) {
        try {
            account.setFirstName(request.getFirstName());
            account.setLastName(request.getLastName());
            account.setBirthDate(request.getBirthDate());
            account.setGender(request.getGender());
            account.setHometown(request.getHometown());
            account.setPhoneNumber(request.getPhoneNumber());

            if (request.getFacultyId() != null) {
                Faculty faculty = facultyRepository.findById(request.getFacultyId())
                        .orElseThrow(() -> new ApiException(ErrorCode.BAD_REQUEST.getStatusCode().value(),"Faculty not found with ID: " + request.getFacultyId()));
                account.setFaculty(faculty);
            }

            account.setMajor(request.getMajor());
            account.setEnrollmentYear(request.getEnrollmentYear());
            account.setClassNumber(String.valueOf(request.getClassNumber()));
        } catch (Exception e) {
            throw new RuntimeException("Error updating personal info: " + e.getMessage(), e);
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
}
