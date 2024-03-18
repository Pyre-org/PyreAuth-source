package com.pyre.auth.config.authentication;

import com.pyre.auth.entity.EndUser;
import com.pyre.auth.exception.customexception.CustomException;
import com.pyre.auth.repository.EndUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final EndUserRepository accountRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Optional<EndUser> account = accountRepository.findByEmail(email);
        if (!account.isPresent()) throw new UsernameNotFoundException("회원 정보를 찾을 수 없습니다.");
        return new CustomUserDetails(account.get());
    }
}
