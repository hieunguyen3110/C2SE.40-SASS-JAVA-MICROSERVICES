package com.capstone1.sasscapstone1.service.UserProfileService;

import com.capstone1.sasscapstone1.dto.DocumentDto.DocumentDto;
import com.capstone1.sasscapstone1.dto.UserProfileResponseDTO.UserProfileResponse;
import com.capstone1.sasscapstone1.entity.Account;
import com.capstone1.sasscapstone1.entity.Documents;
import com.capstone1.sasscapstone1.entity.Faculty;
import com.capstone1.sasscapstone1.entity.Follow;
import com.capstone1.sasscapstone1.repository.Account.AccountRepository;
import com.capstone1.sasscapstone1.repository.Documents.DocumentsRepository;
import com.capstone1.sasscapstone1.repository.Faculty.FacultyRepository;
import com.capstone1.sasscapstone1.repository.Follow.FollowRepository;
import com.capstone1.sasscapstone1.service.UserDetailService.UserDetailServiceImpl;
import com.capstone1.sasscapstone1.util.UserProfileUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserProfileServiceImpl implements UserProfileService {

    private final AccountRepository accountRepository;
    private final UserDetailServiceImpl userDetailService;
    private final FollowRepository followRepository;
    private final DocumentsRepository documentsRepository;
    private final UserProfileUtils userProfileUtils;
    private final FacultyRepository facultyRepository;

    private Account findAccountByEmail(String email) throws Exception {
        return (Account) userDetailService.loadUserByUsername(email);
    }

    @Override
    public UserProfileResponse getUserProfile(String email) throws Exception {
        try {
            Account findAccountByEmail = findAccountByEmail(email);
            Pageable pageable = PageRequest.of(0, 10);
            List<Follow> getAllFollower = followRepository.findByFollowing(findAccountByEmail);
            List<Follow> getAllFollowing = followRepository.findByFollower(findAccountByEmail);
            Page<Documents> getAllDocumentByAccountId = documentsRepository.findAllByAccount_AccountIdAndIsActiveIsTrue(findAccountByEmail.getAccountId(), pageable);
            Optional<Faculty> findFacultyByAccount= facultyRepository.findByAccountsId(findAccountByEmail.getAccountId());
            List<DocumentDto> documentDtos = getAllDocumentByAccountId.stream().map(userProfileUtils::mapToDocumentDto).toList();
            UserProfileResponse response = userProfileUtils.mapToUserProfileResponse(findAccountByEmail, getAllFollower, getAllFollowing);
            response.setDocumentDtos(documentDtos);
            response.setTotalDocument(getAllDocumentByAccountId.getTotalElements());
            response.setTotalPage(getAllDocumentByAccountId.getTotalPages());
            findFacultyByAccount.ifPresent(faculty -> response.setFacultyId(faculty.getFacultyId()));
            return response;
        } catch (Exception e) {
            throw new Exception(e.getMessage(), e);
        }
    }

    @Override
    public UserProfileResponse getUserProfile(String email, Account account) throws Exception {
        try {
            Account findAccountByEmail = findAccountByEmail(email);
            Pageable pageable = PageRequest.of(0, 10);
            List<Follow> getAllFollower = followRepository.findByFollowing(findAccountByEmail);
            List<Follow> getAllFollowing = followRepository.findByFollower(findAccountByEmail);
            Page<Documents> getAllDocumentByAccountId = documentsRepository.findAllByAccount_AccountIdAndIsActiveIsTrue(findAccountByEmail.getAccountId(), pageable);
            List<DocumentDto> documentDtos = getAllDocumentByAccountId.stream().map(userProfileUtils::mapToDocumentDto).toList();
            UserProfileResponse response = userProfileUtils.mapToUserProfileResponse(findAccountByEmail, getAllFollower, getAllFollowing);
            response.setDocumentDtos(documentDtos);
            response.setTotalDocument(getAllDocumentByAccountId.getTotalElements());
            response.setTotalPage(getAllDocumentByAccountId.getTotalPages());
            for (Follow follow : getAllFollower) {
                if (follow.getFollower().getAccountId() == account.getAccountId()) {
                    response.setIsFollow(true);
                    break;
                }
            }
            return response;
        } catch (Exception e) {
            throw new Exception(e.getMessage(), e);
        }
    }
}
