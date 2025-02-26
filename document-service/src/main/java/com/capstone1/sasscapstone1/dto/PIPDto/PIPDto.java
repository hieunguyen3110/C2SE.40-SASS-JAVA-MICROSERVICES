package com.capstone1.sasscapstone1.dto.PIPDto;

import com.capstone1.sasscapstone1.entity.Account;
import lombok.Data;

import java.time.LocalDate;

@Data
public class PIPDto {
    private String firstName;
    private String lastName;
    private String email;
    private String profilePicture;
    private LocalDate birthDate;
    private String gender;
    private String hometown;
    private String phoneNumber;
    private String facultyName;
    private String major;
    private Integer enrollmentYear;
    private Integer classNumber;
}
