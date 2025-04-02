package com.capstone1.sasscapstone1.dto.AccountRatingDto;

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
