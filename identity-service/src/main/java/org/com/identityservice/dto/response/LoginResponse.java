package org.com.identityservice.dto.response;

import lombok.*;

import java.util.Set;

@Builder
@Getter
@Setter
@AllArgsConstructor
public class LoginResponse {
    private long accountId;
    private Set<String> listRoles;
    private String username;
    private String profilePicture;
    private int follower;
    private int following;
    private int upload;
}
