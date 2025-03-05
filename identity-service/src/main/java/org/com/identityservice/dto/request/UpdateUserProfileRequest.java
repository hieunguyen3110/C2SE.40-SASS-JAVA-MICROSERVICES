package org.com.identityservice.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class UpdateUserProfileRequest {
    private String firstName;
    private String lastName;
    private LocalDate birthDate;
    private String gender;
    private String hometown;
    private String phoneNumber;
    private Long facultyId;
    private String major;
    private Integer enrollmentYear;
    private String classNumber;
}

