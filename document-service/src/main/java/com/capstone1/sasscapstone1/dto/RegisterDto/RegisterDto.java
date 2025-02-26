package com.capstone1.sasscapstone1.dto.RegisterDto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class RegisterDto {
    // Getters and Setters
    private String email;
    private String password;
    private String confirmPassword;
    private String confirmationCode;

}
