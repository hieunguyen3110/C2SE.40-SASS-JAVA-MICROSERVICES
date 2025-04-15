package org.com.batchservice.dto.response;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountDto {
    private long accountId;
    private String email;
    private Boolean isActive;
}
