package com.capstone1.sasscapstone1.request;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ResetPasswordRequest {
    private String email;
    private String password;
    private String otp;
}
