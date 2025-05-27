package org.com.identityservice.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.com.identityservice.dto.request.AnalyzeRequest;
import lombok.extern.slf4j.Slf4j;
import org.com.identityservice.dto.request.EnableAnalyzeRequest;
import org.com.identityservice.dto.request.UpdateUserProfileRequest;
import org.com.identityservice.dto.response.*;
import org.com.identityservice.entity.Account;
import org.com.identityservice.entity.Analyze;
import org.com.identityservice.entity.Role;
import org.com.identityservice.enums.ErrorCode;
import org.com.identityservice.exception.ApiException;
import org.com.identityservice.helpers.CreateApiResponse;
import org.com.identityservice.helpers.UserDetailServiceCustom;
import org.com.identityservice.mapper.AccountMapper;
import org.com.identityservice.repository.AccountRepository;
import org.com.identityservice.repository.AnalyzeRepository;
import org.com.identityservice.repository.httpClient.DocumentClient;
import org.com.identityservice.repository.httpClient.ELearningClient;
import org.com.identityservice.repository.httpClient.RecommendationClient;
import org.com.identityservice.repository.httpClient.StudyGroupClient;
import org.com.identityservice.service.AccountService;
import org.com.identityservice.service.FirebaseService;
import org.com.identityservice.service.RedisService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {
    private final AccountRepository accountRepository;
    private final AnalyzeRepository analyzeRepository;
    private final UserDetailServiceCustom userDetailServiceCustom;
    private final FirebaseService firebaseService;
    private final DocumentClient documentClient;
    private final RedisService redisService;
    private final ObjectMapper objectMapper;
    private final ELearningClient eLearningClient;
    private final RecommendationClient recommendationClient;
    private final StudyGroupClient studyGroupClient;
    private final EntityManager entityManager;
    private List<Account> newAccounts;
    private String uploadProfilePicture(MultipartFile profilePicture) {
        try {
            String originalFileName = profilePicture.getOriginalFilename();
            if (originalFileName.isBlank()) {
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

    private void updatePersonalInfo(AccountDto account, UpdateUserProfileRequest request) {
        try {
            account.setFirstName(request.getFirstName());
            account.setLastName(request.getLastName());
            account.setBirthDate(request.getBirthDate());
            account.setGender(request.getGender());
            account.setHometown(request.getHometown());
            account.setPhoneNumber(request.getPhoneNumber());

            if (request.getFacultyId() != null) {
                FacultyDto facultyDto= documentClient.getFacultyById(request.getFacultyId()).getData();
                account.setFacultyId(facultyDto.getFacultyId());
            }
            account.setMajor(request.getMajor());
            account.setEnrollmentYear(request.getEnrollmentYear());
            account.setClassNumber(String.valueOf(request.getClassNumber()));
        } catch (Exception e) {
            throw new RuntimeException("Error updating personal info: " + e.getMessage(), e);
        }
    }

    private UserProfileResponse mapToUserProfileResponse(Account account) {
        List<String> roles = new ArrayList<>();
        for (Role roleEntity : account.getRoles()) {
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
        return response;
    }

    @Override
    public Page<AccountDto> listUsers(int page, int size) {
        try {
            return accountRepository.findAll(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "updatedAt")))
                    .map(account -> {
                        AccountDto accountDto= AccountMapper.mapToAccountDto(account);
                        accountDto.setPassword(null);
                        return accountDto;
                    });
        } catch (Exception e) {
            throw new RuntimeException("Error listing users: " + e.getMessage(), e);
        }
    }

    @Override
    public List<AccountDto> getAllNewUserByDay() throws Exception {
        try{
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime startDate= now.minusDays(2);
            newAccounts=accountRepository.findAllByCreatedAtAndIsActive(startDate,now,false);
            return newAccounts.stream()
                    .map(account->{
                        AccountDto accountDto= AccountMapper.mapToAccountDto(account);
                        accountDto.setPassword(null);
                        return accountDto;
                    })
                    .toList();
        }catch (Exception e){
            throw new Exception(e.getMessage());
        }
    }

    @Override
    public List<AccountDto> getAllNewUserByDayIsActiveIsTrue() throws Exception {
        try{
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime startDate= now.minusDays(2);
            List<Account> accounts=accountRepository.findAllByCreatedAtAndIsActive(startDate,now,true);
            return accounts.stream()
                    .map(account->{
                        AccountDto accountDto= AccountMapper.mapToAccountDto(account);
                        accountDto.setPassword(null);
                        return accountDto;
                    })
                    .toList();
        }catch (Exception e){
            throw new Exception(e.getMessage());
        }
    }

    @Override
    public List<AccountDto> getAllAccountIsAnalyze() throws Exception {
        try{
            LocalDate now= LocalDate.now();
            if(now.getDayOfWeek().getValue() == 4){
                List<Account> accountList= accountRepository.findAllByIsAnalyzeIsTrue();
                return accountList.stream()
                        .map(account->AccountDto.builder()
                                .accountId(account.getAccountId())
                                .build())
                        .toList();
            }else{
                return new ArrayList<>();
            }
        }catch (Exception e){
            throw new Exception(e.getMessage());
        }
    }

    @Override
    public AccountDto getUserDetails(Long accountId) {
        try {
            Account account = accountRepository.findById(accountId)
                    .orElseThrow(() -> new ApiException(ErrorCode.BAD_REQUEST.getStatusCode().value(),"User not found with ID: " + accountId));
            AccountDto accountDto= AccountMapper.mapToAccountDto(account);
            accountDto.setPassword(null);
            return accountDto;
        } catch (ApiException e) {
            throw new ApiException(e.getCode(),"Error fetching user details: " + e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException("An unexpected error occurred while fetching user details: " + e.getMessage(), e);
        }
    }

    @Override
    public AccountDto getUserDetails(String email) {
        try {
            Account account= (Account) userDetailServiceCustom.loadUserByUsername(email);
            AccountDto accountDto= AccountMapper.mapToAccountDto(account);
            accountDto.setPassword(null);
            return accountDto;
        } catch (ApiException e) {
            throw new ApiException(e.getCode(),"Error fetching user details: " + e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException("An unexpected error occurred while fetching user details: " + e.getMessage(), e);
        }
    }

    @Override
    public Long countStatsByRoleName(String roleName) {
        try {
            return accountRepository.countByRolesName(roleName);
        } catch (ApiException e) {
            throw new ApiException(e.getCode(),"Error count stats role: " + e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException("An unexpected error occurred while fetching user details: " + e.getMessage(), e);
        }
    }

    @Override
    public ApiResponse<String> allowActiveAccount(String email) throws Exception {
        try{
            Authentication authentication= SecurityContextHolder.getContext().getAuthentication();
            if(!(authentication instanceof AnonymousAuthenticationToken)){
                Optional<Account> findAccount= accountRepository.findAccountByEmail(email.toLowerCase());
                if (findAccount.isPresent()){
                    Account account= findAccount.get();
                    account.setIsActive(true);
                    accountRepository.save(account);
                    return CreateApiResponse.createResponse("Update account successful",false);
                }else{
                    throw new ApiException(ErrorCode.BAD_REQUEST.getStatusCode().value(),"Account not found by email: "+email);
                }
            }else{
                throw new ApiException(ErrorCode.FORBIDDEN.getStatusCode().value(),"Account not allowed");
            }
        }catch (Exception e){
            throw new Exception(e.getMessage());
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

//    @Override
//    public ApiResponse<List<AccountRatingDto>> getAccountByAccountIds(List<Long> accountIds,String docTitle, long docId) throws Exception {
//        try{
//            List<Account> getAllAccount= accountRepository.findAllByAccountIdIn(accountIds);
//            String key= docTitle+"_"+docId;
//            String isExistJson= (String) redisService.getData(key);
//            List<AccountRatingDto> accountRatingDtos;
//            if(isExistJson!=null){
//                accountRatingDtos= objectMapper.readValue(isExistJson, new TypeReference<>() {});
//                getAllAccount.forEach(account->{
//                    accountRatingDtos.add(
//                            AccountRatingDto.builder()
//                                    .accountId(account.getAccountId())
//                                    .firstName(account.getFirstName())
//                                    .lastName(account.getLastName())
//                                    .profilePicture(account.getProfilePicture())
//                                    .build()
//                    );
//                });
//            }else{
//                accountRatingDtos = getAllAccount.stream().map(account->
//                        AccountRatingDto.builder()
//                                .accountId(account.getAccountId())
//                                .firstName(account.getFirstName())
//                                .lastName(account.getLastName())
//                                .profilePicture(account.getProfilePicture())
//                                .build()
//                ).toList();
//            }
//            String json= objectMapper.writeValueAsString(accountRatingDtos);
//            redisService.saveData(key,json,864000);
//            return CreateApiResponse.createResponse(accountRatingDtos,false);
//        }catch (Exception e){
//            throw new Exception("Error when get all account with accountIds: "+ accountIds.toString());
//        }
//    }

    @Override
    public ApiResponse<String> updateListAccountRatingAtRedis(List<Long> accountIds, String docTitle, long docId, long rating) throws Exception {
        try{
            String key= docTitle+"_"+docId;
            String isExistJson= (String) redisService.getData(key);
            List<AccountRatingDto> accountRatingDtos;
            if(isExistJson!=null){
                List<Account> getAllAccount= accountRepository.findAllByAccountIdIn(accountIds);
                accountRatingDtos= objectMapper.readValue(isExistJson, new TypeReference<>() {});
                getAllAccount.forEach(account->{
                    accountRatingDtos.add(
                            AccountRatingDto.builder()
                                    .accountId(account.getAccountId())
                                    .rating(rating)
                                    .build()
                    );
                });
                Long ttl= redisService.getTtl(key);
                String json= objectMapper.writeValueAsString(accountRatingDtos);
                redisService.saveData(key,json,ttl);
            }
            return CreateApiResponse.createResponse("Update account rating in redis is successful",false);
        }catch (Exception e){
            throw new Exception("Error when get all account with accountIds: "+ accountIds.toString());
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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String approveNewUsers(Long accountId) {
        try {
            newAccounts.forEach(account->{
                if(account.getAccountId()==accountId){
                    account.setIsActive(true);
                    accountRepository.save(account);
                }
            });
            return "Users approved successfully.";
        } catch (Exception e) {
            throw new ApiException(ErrorCode.BAD_GATEWAY.getStatusCode().value(),"Error approving users: " + e.getMessage());
        }
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

    @Override
    public ApiResponse<List<SearchUserResponseDto>> searchUsersByName(String name, Long loggedInAccountId, int pageNum, int pageSize) {
        try {
            Pageable pageable = PageRequest.of(pageNum, pageSize);
            Page<Account> accountPage = accountRepository.findAccountByFirstNameAndLastName(name,name,loggedInAccountId, pageable);

            List<SearchUserResponseDto> searchResults = accountPage.getContent().stream().map(account -> {
                SearchUserResponseDto dto = new SearchUserResponseDto();
                dto.setFirstName(account.getFirstName());
                dto.setLastName(account.getLastName());
                dto.setEmail(account.getEmail());
                dto.setProfilePicture(account.getProfilePicture());
                dto.setMajor(account.getMajor());

                return dto;
            }).collect(Collectors.toList());

            return CreateApiResponse.createResponse(searchResults,false);
        } catch (Exception e) {
            throw new RuntimeException("Error searching users: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public ApiResponse<UserProfileResponse> updateUserProfile(AccountDto accountDto, UpdateUserProfileRequest request, MultipartFile profilePicture) {
        try {
            // Cập nhật thông tin cá nhân
            updatePersonalInfo(accountDto, request);

            // Xử lý ảnh đại diện nếu có
            if (profilePicture != null && !profilePicture.isEmpty()) {
                // Xóa ảnh đại diện cũ nếu có
                String oldProfilePictureUrl = accountDto.getProfilePicture();
                if (oldProfilePictureUrl != null && !oldProfilePictureUrl.isEmpty()) {
                    deleteProfilePicture(accountDto.getAccountId());
                }

                // Tải ảnh đại diện mới lên
                String profilePictureUrl = uploadProfilePicture(profilePicture);
                accountDto.setProfilePicture(profilePictureUrl);
            }
            Account mapToAccount= AccountMapper.mapToAccount(accountDto);
            // Lưu tài khoản sau khi cập nhật
            Account updatedAccount = accountRepository.save(mapToAccount);
            return CreateApiResponse.createResponse(mapToUserProfileResponse(updatedAccount),false);
        } catch (ApiException e) {
            throw new ApiException(e.getCode(),"Update failed: " + e.getMessage());
        } catch (RuntimeException e) {
            throw new RuntimeException("Unexpected error during profile update: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException("General error during profile update: " + e.getMessage(), e);
        }
    }

    // Hàm xóa ảnh đại diện cho quản trị viên
    @Transactional
    @Override
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

    @Override
    public String enableStudyAnalyze(AccountDto accountDto) throws Exception {
        try{
            Account account= accountRepository.findByAccountId(accountDto.getAccountId())
                    .orElseThrow(()->new ApiException(ErrorCode.BAD_REQUEST.getStatusCode().value(), "Account not found"));
            account.setIsAnalyze(true);
            account.setLastAnalyzeTime(LocalDateTime.now());
            accountRepository.save(account);
            return "enable study analyze is successful";
        }catch (Exception e){
            throw new Exception(e);
        }
    }

    @Override
    public String disableStudyAnalyze(AccountDto accountDto) throws Exception {
        try{
            Account account= accountRepository.findByAccountId(accountDto.getAccountId())
                    .orElseThrow(()->new ApiException(ErrorCode.BAD_REQUEST.getStatusCode().value(), "Account not found"));
            account.setIsAnalyze(false);
            accountRepository.save(account);
            return "Disable study analyze is successful";
        }catch (Exception e){
            throw new Exception(e);
        }
    }

    @Override
    public String saveCoursePeriod(EnableAnalyzeRequest request, AccountDto accountDto) throws Exception {
        try{
            String response= eLearningClient.saveCoursePeriod(request).getData();
            if(response!=null){
                return "Bạn đã bật chức năng phân tích học tập bằng AI, Hãy hoàn thành các bài tập và các bài thi của các môn học trong kì. Những môn học sẽ được phân tích vào thứ 2 hàng tuần.";
//                LocalDateTime now = LocalDateTime.now();
//                if(now.getDayOfWeek().getValue() == 1){
//                    AnalyzeData analyzeData= eLearningClient.getAnalyzeDataByAccountId(accountDto.getAccountId()).getData();
//                    if(analyzeData!=null){
//                        Boolean checkParticipateGroup= studyGroupClient.checkAccountIsParticipateGroup(accountDto.getAccountId()).getData();
//                        AnalyzeRequest analyzeRequest= AnalyzeRequest.builder()
//                                .Assignment_Completion_Rate(List.of(analyzeData.getAssignmentScore()))
//                                .Exam_Score(List.of(analyzeData.getExamScore()))
//                                .Online_Courses_Completed(List.of(analyzeData.getOnlineCourseComplete()+analyzeData.getOnlineTestComplete()))
//                                .Participation_in_Discussions(List.of(checkParticipateGroup?"Yes":"No"))
//                                .build();
//                        String message= recommendationClient.getSolutionByAI(analyzeRequest).getData();
//                        Optional<Analyze> analyzeExist= analyzeRepository.findByAccount_AccountId(accountDto.getAccountId());
//                        Account accountMapper= AccountMapper.mapToAccount(accountDto);
//                        Account accountEntity= entityManager.merge(accountMapper);
//                        Analyze analyze;
//                        if(analyzeExist.isEmpty()){
//                            analyze= Analyze.builder()
//                                    .account(accountEntity)
//                                    .message(message)
//                                    .build();
//                        }else{
//                            analyze= analyzeExist.get();
//                            analyze.setMessage(message);
//                        }
//                        analyzeRepository.save(analyze);
//                        return message;
//                    }else{
//                        return null;
//                    }
//                }else{
//
//                }

            }else{
                throw new ApiException(ErrorCode.SERVICE_UNAVAILABLE.getStatusCode().value(),"Having error when try leaning analytic");
            }
        }catch (Exception e){
            throw new Exception(e);
        }
    }

    @Override
    public LearningAnalyzeResponse getAnalyzeData(AccountDto accountDto) throws Exception {
        try{
            Optional<Analyze> analyzeOpt = analyzeRepository.findByAccount_AccountId(accountDto.getAccountId());
            if(analyzeOpt.isPresent()){
                Analyze analyze = analyzeOpt.get();
                return objectMapper.readValue(analyze.getMessage(), LearningAnalyzeResponse.class);
            }else{
                return null;
            }
        }catch(Exception e){
            throw new Exception(e);
        }
    }

    @Override
    public List<AccountDto> findByIds(Set<Long> accountIds) {
        try {
            if (accountIds == null || accountIds.isEmpty()) {
                return new ArrayList<>();
            }

            List<AccountDto> cachedAccounts = new ArrayList<>();
            List<Long> missingIds = new ArrayList<>();
            for (Long accountId : accountIds) {
                String cacheKey = "account:" + accountId;
                String cachedData = (String) redisService.getData(cacheKey);
                if (cachedData != null) {
                    AccountDto accountDto = objectMapper.readValue(cachedData, AccountDto.class);
                    cachedAccounts.add(accountDto);
                } else {
                    missingIds.add(accountId);
                }
            }

            if (!missingIds.isEmpty()) {
                List<Account> accounts = accountRepository.findAllById(missingIds);
                List<AccountDto> fetchedAccounts = accounts.stream()
                        .map(account -> {
                            AccountDto accountDto = AccountMapper.mapToAccountDto(account);
                            accountDto.setPassword(null);
                            String cacheKey = "account:" + accountDto.getAccountId();
                            try {
                                String json = objectMapper.writeValueAsString(accountDto);
                                redisService.saveData(cacheKey, json, 30 * 24 * 60 * 60);
                            } catch (Exception e) {
                                log.error("Error saving account to cache: {}", e.getMessage());
                            }
                            return accountDto;
                        })
                        .toList();
                cachedAccounts.addAll(fetchedAccounts);
            }

            return cachedAccounts;
        } catch (Exception e) {
            throw new RuntimeException("Error fetching accounts by IDs: " + e.getMessage(), e);
        }
    }
}
