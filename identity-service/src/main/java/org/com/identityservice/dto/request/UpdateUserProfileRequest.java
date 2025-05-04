package org.com.identityservice.dto.request;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@Builder
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

