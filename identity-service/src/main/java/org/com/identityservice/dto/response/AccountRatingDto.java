package org.com.identityservice.dto.response;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AccountRatingDto {
    private long accountId;
    private String firstName;
    private String lastName;
    private String profilePicture;
}
