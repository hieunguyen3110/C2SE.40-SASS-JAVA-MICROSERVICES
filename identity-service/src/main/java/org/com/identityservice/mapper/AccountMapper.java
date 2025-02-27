package org.com.identityservice.mapper;

import org.com.identityservice.dto.response.AccountDto;
import org.com.identityservice.entity.Account;

public class AccountMapper{
    public static Account mapToAccount(AccountDto accountDto){
        return Account.builder()
                .accountId(accountDto.getAccountId())
                .email(accountDto.getEmail())
                .firstName(accountDto.getFirstName())
                .lastName(accountDto.getLastName())
                .profilePicture(accountDto.getProfilePicture())
                .birthDate(accountDto.getBirthDate())
                .gender(accountDto.getGender())
                .hometown(accountDto.getHometown())
                .phoneNumber(accountDto.getPhoneNumber())
                .major(accountDto.getMajor())
                .enrollmentYear(accountDto.getEnrollmentYear())
                .classNumber(accountDto.getClassNumber())
                .isDeleted(accountDto.getIsDeleted())
                .isActive(accountDto.getIsActive())
                .roles(accountDto.getRoles())
                .build();
    }
    public static AccountDto mapToAccountDto(Account account){
        return AccountDto.builder()
                .accountId(account.getAccountId())
                .email(account.getEmail())
                .roles(account.getRoles())
                .username(account.getFirstName()+" "+account.getLastName())
                .profilePicture(account.getProfilePicture())
                .build();
    }
}
