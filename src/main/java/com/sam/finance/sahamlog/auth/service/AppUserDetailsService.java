package com.sam.finance.sahamlog.auth.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.sam.finance.sahamlog.auth.repository.AppUserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AppUserDetailsService implements UserDetailsService {

    private final AppUserRepository appUserRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return appUserRepository.findByEmailIgnoreCase(username)
            .map(user -> new AuthenticatedUser(user.getId(), user.getEmail(), user.getPasswordHash()))
            .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }
}
