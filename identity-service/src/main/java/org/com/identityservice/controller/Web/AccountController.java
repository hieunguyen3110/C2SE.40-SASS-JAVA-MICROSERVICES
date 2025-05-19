package org.com.identityservice.controller.Web;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.com.identityservice.dto.request.EnableAnalyzeRequest;
import org.com.identityservice.dto.request.UpdateUserProfileRequest;
import org.com.identityservice.dto.response.*;
import org.com.identityservice.entity.Account;
import org.com.identityservice.enums.ErrorCode;
import org.com.identityservice.exception.ApiException;
import org.com.identityservice.helpers.CreateApiResponse;
import org.com.identityservice.producer.UserUpdateProducer;
import org.com.identityservice.service.AccountService;
import org.com.identityservice.service.RedisService;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Nullable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@RestController("webAccountController")
@RequestMapping("/account")
@RequiredArgsConstructor
public class AccountController {
    private final AccountService accountService;
    private final RedisService redisService;
    private final ObjectMapper objectMapper;
    private final UserUpdateProducer userUpdateProducer;

    private void updateAccountDto(AccountDto accountDto,UserProfileResponse userProfileResponse){
        accountDto.setFirstName(userProfileResponse.getFirstName());
        accountDto.setLastName(userProfileResponse.getLastName());
        accountDto.setEmail(userProfileResponse.getEmail());
        accountDto.setProfilePicture(userProfileResponse.getProfilePicture());
        accountDto.setBirthDate(userProfileResponse.getBirthDate());
        accountDto.setGender(userProfileResponse.getGender());
        accountDto.setHometown(userProfileResponse.getHometown());
        accountDto.setPhoneNumber(userProfileResponse.getPhoneNumber());
        accountDto.setMajor(userProfileResponse.getMajor());
        accountDto.setEnrollmentYear(userProfileResponse.getEnrollmentYear());
        accountDto.setClassNumber(userProfileResponse.getClassNumber());
    }

    @GetMapping("/{accountId}")
    public ApiResponse<AccountDto> getUserDetails(@PathVariable Long accountId) {
        return CreateApiResponse.createResponse(accountService.getUserDetails(accountId),false);
    }
    @GetMapping("")
    public ApiResponse<AccountDto> getUserDetailsByEmail(@RequestParam("email") String email) {
        return CreateApiResponse.createResponse(accountService.getUserDetails(email),false);
    }
    @GetMapping("/search-by-name")
    public ApiResponse<List<SearchUserResponseDto>> searchUsersByName(@RequestParam String name,
                                                                      @RequestParam(defaultValue = "0") int pageNum,
                                                                      @RequestParam(defaultValue = "5") int pageSize) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            AccountDto loggedInAccount = (AccountDto) authentication.getPrincipal();

            return accountService.searchUsersByName(name, loggedInAccount.getAccountId(),pageNum,pageSize);
        } else {
            throw new ApiException(ErrorCode.FORBIDDEN.getStatusCode().value(),"You are not authorized to perform this action.");
        }
    }

    @PutMapping("/update-profile")
    public ApiResponse<UserProfileResponse> updateUserProfile(
            @Nullable @RequestPart("firstName") String firstName,
            @Nullable @RequestPart("lastName") String lastName,
            @Nullable @RequestPart("birthDate") String birthDate,
            @Nullable @RequestPart("gender") String gender,
            @Nullable @RequestPart("hometown") String hometown,
            @Nullable @RequestPart("phoneNumber") String phoneNumber,
            @Nullable @RequestPart("facultyId") String facultyId,
            @Nullable @RequestPart("major") String major,
            @Nullable @RequestPart("enrollmentYear") String enrollmentYear,
            @Nullable @RequestPart("classNumber") String classNumber,
            @Nullable @RequestPart(value = "profilePicture", required = false) MultipartFile profilePicture, HttpServletRequest httpServletRequest)
    {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            AccountDto account = (AccountDto) authentication.getPrincipal();
            try {
                UpdateUserProfileRequest request = UpdateUserProfileRequest.builder()
                        .firstName(firstName)
                        .lastName(lastName)
                        .birthDate(birthDate != null ? LocalDate.parse(birthDate) : null)
                        .gender(gender)
                        .hometown(hometown)
                        .phoneNumber(phoneNumber)
                        .facultyId((facultyId != null && Long.parseLong(facultyId) != 0) ? Long.parseLong(facultyId) : null)
                        .major(major)
                        .enrollmentYear(enrollmentYear != null ? Integer.parseInt(enrollmentYear) : null)
                        .classNumber(classNumber)
                        .build();
                ApiResponse<UserProfileResponse> response= accountService.updateUserProfile(account, request, profilePicture);
                String authHeader= httpServletRequest.getHeader("Authorization");
                if(authHeader != null){
                    String token= authHeader.substring(7);
                    String json= (String) redisService.getData(token);
                    AccountDto extractAccountFromRedis= objectMapper.readValue(json,AccountDto.class);
                    updateAccountDto(extractAccountFromRedis,response.getData());
                    String accountJson= objectMapper.writeValueAsString(extractAccountFromRedis);
                    userUpdateProducer.sendEventUpdateUser(token,accountJson);
                }
                return response;
            } catch (Exception e) {
                throw new ApiException(ErrorCode.INTERNAL_SERVER_ERROR.getStatusCode().value(),"Error updating profile: " + e.getMessage());
            }
        } else {
            throw new ApiException(ErrorCode.FORBIDDEN.getStatusCode().value(),"You are not authorized to perform this action.");
        }
    }

    @DeleteMapping("/delete-profile-picture")
    public ApiResponse<String> deleteProfilePicture() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            AccountDto account = (AccountDto) authentication.getPrincipal();
            Long accountId = account.getAccountId();

            accountService.deleteProfilePicture(accountId);

            return CreateApiResponse.createResponse("Profile picture deleted successfully.",false);
        } else {
            throw new ApiException(ErrorCode.FORBIDDEN.getStatusCode().value(),"You are not authorized to perform this action.");
        }
    }

    @GetMapping("/accounts/batch")
    public ApiResponse<List<AccountDto>> getAccountsByIds(@RequestBody Set<Long> accountIds) {
        List<AccountDto> accounts = accountService.findByIds(accountIds);
        return CreateApiResponse.createResponse(accounts, false);
    }
//    @PostMapping("/account-rated")
//    public ApiResponse<List<AccountRatingDto>> getAllAccountByRatingDocId(@RequestBody List<Long> accountIds,
//                                                                          @RequestParam("docTitle") String docTitle,
//                                                                          @RequestParam("docId") long docId) throws Exception {
//        return accountService.getAccountByAccountIds(accountIds,docTitle,docId);
//    }

    @GetMapping("/enable-analyze-data")
    public ApiResponse<String> handleEnableStudyAnalyze() throws Exception {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(!(authentication instanceof AnonymousAuthenticationToken)){
            AccountDto accountDto= (AccountDto) authentication.getPrincipal();
            return CreateApiResponse.createResponse(accountService.enableStudyAnalyze(accountDto), false);
        }else{
            throw new ApiException(ErrorCode.FORBIDDEN.getStatusCode().value(),"Token is expires");
        }
    }
    @GetMapping("/disable-analyze-data")
    public ApiResponse<String> handleDisableStudyAnalyze() throws Exception {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(!(authentication instanceof AnonymousAuthenticationToken)){
            AccountDto accountDto= (AccountDto) authentication.getPrincipal();
            return CreateApiResponse.createResponse(accountService.disableStudyAnalyze(accountDto), false);
        }else{
            throw new ApiException(ErrorCode.FORBIDDEN.getStatusCode().value(),"Token is expires");
        }
    }
    @PostMapping("/save-course-period")
    public ApiResponse<String> handleSaveCoursePeriod(@RequestBody EnableAnalyzeRequest request) throws Exception {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(!(authentication instanceof AnonymousAuthenticationToken)){
            AccountDto accountDto= (AccountDto) authentication.getPrincipal();
            return CreateApiResponse.createResponse(accountService.saveCoursePeriod(request,accountDto), false);
        }else{
            throw new ApiException(ErrorCode.FORBIDDEN.getStatusCode().value(),"Token is expires");
        }
    }
    @GetMapping("/get-analyze")
    public ApiResponse<LearningAnalyzeResponse> handleGetAnalyze() throws Exception {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(!(authentication instanceof AnonymousAuthenticationToken)){
            AccountDto accountDto= (AccountDto) authentication.getPrincipal();
            return CreateApiResponse.createResponse(accountService.getAnalyzeData(accountDto), false);
        }else{
            throw new ApiException(ErrorCode.FORBIDDEN.getStatusCode().value(),"Token is expires");
        }
    }
}
