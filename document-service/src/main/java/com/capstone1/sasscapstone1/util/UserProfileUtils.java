package com.capstone1.sasscapstone1.util;

import com.capstone1.sasscapstone1.dto.DocumentDto.DocumentDto;
import com.capstone1.sasscapstone1.dto.UserProfileResponseDTO.UserProfileResponse;
import com.capstone1.sasscapstone1.entity.*;
import com.capstone1.sasscapstone1.dto.UpdateUserProfileRequestDto.UpdateUserProfileRequest;
import com.capstone1.sasscapstone1.enums.ErrorCode;
import com.capstone1.sasscapstone1.exception.ApiException;
import com.capstone1.sasscapstone1.repository.Faculty.FacultyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class UserProfileUtils {

    private final FacultyRepository facultyRepository;

    @Autowired
    public UserProfileUtils(FacultyRepository facultyRepository) {
        this.facultyRepository = facultyRepository;
    }

    public UserProfileResponse mapToUserProfileResponse(Account account, List<Follow> followers, List<Follow> followings) {
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

        if (followers != null) {
            response.setFollower(followers.size());
        }

        if (followings != null) {
            response.setFollowing(followings.size());
        }

        if (account.getFaculty() != null) {
            response.setFacultyName(account.getFaculty().getFacultyName());
        }
        return response;
    }

    public DocumentDto mapToDocumentDto(Documents documents) {
        return DocumentDto.builder()
                .filePath(documents.getFilePath())
                .title(documents.getTitle())
                .docId(documents.getDocId())
                .facultyName(documents.getFaculty().getFacultyName())
                .type(documents.getType())
                .subjectName(documents.getSubject().getSubjectName())
                .description(documents.getDescription())
                .build();
    }

    public void updatePersonalInfo(Account account, UpdateUserProfileRequest request) {
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
}
