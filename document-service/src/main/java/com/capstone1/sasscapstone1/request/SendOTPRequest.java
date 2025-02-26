package com.capstone1.sasscapstone1.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SendOTPRequest {
    private String otp;
    private String email;
}
