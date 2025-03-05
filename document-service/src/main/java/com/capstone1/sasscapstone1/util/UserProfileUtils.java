package com.capstone1.sasscapstone1.util;

import com.capstone1.sasscapstone1.dto.AccountDto.AccountDto;
import com.capstone1.sasscapstone1.dto.DocumentDto.DocumentDto;
import com.capstone1.sasscapstone1.dto.RoleDto.RoleDto;
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

    public UserProfileResponse mapToUserProfileResponse(AccountDto account, List<Follow> followers, List<Follow> followings) {
        List<String> roles = new ArrayList<>();
        for (RoleDto roleEntity : account.getRoles()) {
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

//        if (account.getFaculty() != null) {
//            response.setFacultyName(account.getFaculty().getFacultyName());
//        }
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
}
