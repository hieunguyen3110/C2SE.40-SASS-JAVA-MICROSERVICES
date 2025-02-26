package com.capstone1.sasscapstone1.service.UserDetailService;

import com.capstone1.sasscapstone1.entity.Account;
import com.capstone1.sasscapstone1.enums.ErrorCode;
import com.capstone1.sasscapstone1.exception.ApiException;
import com.capstone1.sasscapstone1.repository.Account.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserDetailServiceImpl implements UserDetailsService {
    private final AccountRepository accountRepository;
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        try{
            Optional<Account> findAccount = accountRepository.findAccountByEmailAndIsActive(username,true);
            if(findAccount.isEmpty()){
                throw new UsernameNotFoundException("Account not found with email or not active: "+ username);
            }
            return findAccount.get();
        }catch (UsernameNotFoundException e){
            throw new ApiException(ErrorCode.BAD_REQUEST.getStatusCode().value(),e.getMessage());
        }
    }
}
