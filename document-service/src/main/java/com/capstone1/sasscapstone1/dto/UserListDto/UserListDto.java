package com.capstone1.sasscapstone1.dto.UserListDto;

import lombok.Data;

@Data
public class UserListDto {
    private Long accountId;
    private String firstName;
    private String lastName;
    private String email;
    private String birthDate;
    private String gender;
    private String hometown;
    private String role;
    private Boolean isActive;
}
