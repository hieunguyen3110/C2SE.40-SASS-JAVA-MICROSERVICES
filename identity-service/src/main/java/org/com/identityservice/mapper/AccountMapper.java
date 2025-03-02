package org.com.identityservice.mapper;

import org.com.identityservice.dto.response.AccountDto;
import org.com.identityservice.entity.Account;

public class AccountMapper{
    public static Account mapToAccount(AccountDto accountDto){
        return Account.builder()
                .accountId(accountDto.getAccountId())
                .email(accountDto.getEmail())
                .password(accountDto.getPassword())
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
                .password(account.getPassword())
                .roles(account.getRoles())
                .username(account.getFirstName()+" "+account.getLastName())
                .profilePicture(account.getProfilePicture())
                .firstName(account.getFirstName())
                .lastName(account.getLastName())
                .birthDate(account.getBirthDate())
                .gender(account.getGender())
                .hometown(account.getHometown())
                .phoneNumber(account.getPhoneNumber())
                .major(account.getMajor())
                .enrollmentYear(account.getEnrollmentYear())
                .classNumber(account.getClassNumber())
                .isDeleted(account.getIsDeleted())
                .isActive(account.getIsActive())
                .build();
    }
}
