package com.capstone1.sasscapstone1.dto.UpdateUserProfileRequestDto;

import lombok.Data;
import java.time.LocalDate;

@Data
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
