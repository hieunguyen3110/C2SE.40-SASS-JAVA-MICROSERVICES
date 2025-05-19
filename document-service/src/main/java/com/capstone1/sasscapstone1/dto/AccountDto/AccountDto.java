package com.capstone1.sasscapstone1.dto.AccountDto;

import com.capstone1.sasscapstone1.dto.RoleDto.RoleDto;
import lombok.*;
import java.time.LocalDate;
import java.util.Set;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountDto {
    private long accountId;
    private String email;
    private String password;
    private String firstName;
    private String lastName;
    private String username;
    private String profilePicture;
    private LocalDate birthDate;
    private String gender;
    private String hometown;
    private String phoneNumber;
    private Long facultyId;
    private String major;
    private Integer enrollmentYear;
    private String classNumber;
    private Boolean isDeleted;
    private Boolean isActive;
    private Boolean isEnableAnalyze;
    private Set<RoleDto> roles;
}
