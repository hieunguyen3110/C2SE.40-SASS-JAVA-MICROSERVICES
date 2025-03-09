package org.com.notificationservice.dto.response;

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
    private long facultyId;
    private String major;
    private Integer enrollmentYear;
    private String classNumber;
    private Boolean isDeleted;
    private Boolean isActive;
    private Set<RoleDto> roles;
}
