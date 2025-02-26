package com.capstone1.sasscapstone1.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ChangePasswordRequest {
    private String newPassword;
    private String oldPassword;
}
