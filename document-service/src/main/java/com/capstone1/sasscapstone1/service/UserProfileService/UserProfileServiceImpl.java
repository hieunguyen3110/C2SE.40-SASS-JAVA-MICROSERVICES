package com.capstone1.sasscapstone1.service.UserProfileService;

import com.capstone1.sasscapstone1.dto.AccountDto.AccountDto;
import com.capstone1.sasscapstone1.dto.CoursePeriodDto.CoursePeriodDto;
import com.capstone1.sasscapstone1.dto.DocumentDto.DocumentDto;
import com.capstone1.sasscapstone1.dto.RoleDto.RoleDto;
import com.capstone1.sasscapstone1.dto.UserProfileResponseDTO.UserProfileResponse;
import com.capstone1.sasscapstone1.entity.Documents;
import com.capstone1.sasscapstone1.entity.Faculty;
import com.capstone1.sasscapstone1.entity.Follow;
import com.capstone1.sasscapstone1.repository.Documents.DocumentsRepository;
import com.capstone1.sasscapstone1.repository.Faculty.FacultyRepository;
import com.capstone1.sasscapstone1.repository.Follow.FollowRepository;
import com.capstone1.sasscapstone1.repository.httpClient.ELearningClient;
import com.capstone1.sasscapstone1.repository.httpClient.IdentityClient;
import com.capstone1.sasscapstone1.util.UserProfileUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserProfileServiceImpl implements UserProfileService {
    private final FollowRepository followRepository;
    private final DocumentsRepository documentsRepository;
    private final UserProfileUtils userProfileUtils;
    private final FacultyRepository facultyRepository;
    private final IdentityClient identityClient;
    private final ELearningClient eLearningClient;

    private AccountDto findAccountByEmail(String email) throws Exception {
        return identityClient.getAccountEmail(email).getData();
    }

    @Override
    public UserProfileResponse getUserProfile(AccountDto accountDto) throws Exception {
        try {
            Pageable pageable = PageRequest.of(0, 10);
            List<Follow> getAllFollower = followRepository.findByFollowingId(accountDto.getAccountId());
            List<Follow> getAllFollowing = followRepository.findByFollowerId(accountDto.getAccountId());
            Page<Documents> getAllDocumentByAccountId = documentsRepository.findAllByAccountIdAndIsActiveIsTrue(accountDto.getAccountId(), pageable);
//            Optional<Faculty> findFacultyByAccount= facultyRepository.findByFacultyId(accountDto.getAccountId());
            List<DocumentDto> documentDtos = getAllDocumentByAccountId.stream().map(userProfileUtils::mapToDocumentDto).toList();
            UserProfileResponse response = userProfileUtils.mapToUserProfileResponse(accountDto, getAllFollower, getAllFollowing);
            response.setDocumentDtos(documentDtos);
            response.setTotalDocument(getAllDocumentByAccountId.getTotalElements());
            response.setTotalPage(getAllDocumentByAccountId.getTotalPages());
            List<RoleDto> convertRoleToList= new ArrayList<>(accountDto.getRoles());
            if(Objects.equals(convertRoleToList.getFirst().getName(), "STUDENT")){
                CoursePeriodDto coursePeriodDto= eLearningClient.checkExistCoursePeriod(accountDto.getAccountId()).getData();
                response.setCoursePeriodDto(coursePeriodDto);
            }
//            findFacultyByAccount.ifPresent(faculty -> response.setFacultyId(faculty.getFacultyId()));
            return response;
        } catch (Exception e) {
            throw new Exception(e.getMessage(), e);
        }
    }

    @Override
    public UserProfileResponse getUserProfile(String email, AccountDto account) throws Exception {
        try {
            AccountDto findAccountByEmail = findAccountByEmail(email);
            Pageable pageable = PageRequest.of(0, 10);
            List<Follow> getAllFollower = followRepository.findByFollowingId(findAccountByEmail.getAccountId());
            List<Follow> getAllFollowing = followRepository.findByFollowerId(findAccountByEmail.getAccountId());
            Page<Documents> getAllDocumentByAccountId = documentsRepository.findAllByAccountIdAndIsActiveIsTrue(findAccountByEmail.getAccountId(), pageable);
            List<DocumentDto> documentDtos = getAllDocumentByAccountId.stream().map(userProfileUtils::mapToDocumentDto).toList();
            UserProfileResponse response = userProfileUtils.mapToUserProfileResponse(findAccountByEmail, getAllFollower, getAllFollowing);
            response.setDocumentDtos(documentDtos);
            response.setTotalDocument(getAllDocumentByAccountId.getTotalElements());
            response.setTotalPage(getAllDocumentByAccountId.getTotalPages());
            for (Follow follow : getAllFollower) {
                if (follow.getFollowerId()== account.getAccountId()) {
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
