package org.com.identityservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class UserProfileResponse {
    private String firstName;
    private String lastName;
    private String email;
    private String profilePicture;
    private LocalDate birthDate;
    private String gender;
    private String hometown;
    private String phoneNumber;
    private String facultyName;
    private Long facultyId;
    private String major;
    private Integer enrollmentYear;
    private String classNumber;
    private int follower;
    private int following;
    private Boolean isFollow= false;
    private List<String> roles;
    private List<DocumentDto> documentDtos;
    private long totalDocument;
    private int totalPage;
}

