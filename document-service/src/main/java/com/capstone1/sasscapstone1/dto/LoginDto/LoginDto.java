package com.capstone1.sasscapstone1.dto.LoginDto;

import com.capstone1.sasscapstone1.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Builder
@Getter
@Setter
@AllArgsConstructor
public class LoginDto {
    private long accountId;
    private String accessToken;
    private String refreshToken;
    private Set<String> listRoles;
    private String username;
    private String profilePicture;
    private int follower;
    private int following;
    private int upload;
}
