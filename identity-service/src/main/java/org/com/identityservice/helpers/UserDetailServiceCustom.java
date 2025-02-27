package org.com.identityservice.helpers;

import lombok.RequiredArgsConstructor;
import org.com.identityservice.repository.AccountRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserDetailServiceCustom implements UserDetailsService {
    private final AccountRepository accountRepository;
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return accountRepository.findAccountByEmailAndIsActive(username, true)
                .orElseThrow(() -> new UsernameNotFoundException("Account not found with email or not active: " + username));
    }
}
