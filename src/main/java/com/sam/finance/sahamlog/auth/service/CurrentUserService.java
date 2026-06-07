package com.sam.finance.sahamlog.auth.service;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.sam.finance.sahamlog.auth.domain.AppUser;
import com.sam.finance.sahamlog.auth.repository.AppUserRepository;
import com.sam.finance.sahamlog.shared.exception.ResourceNotFoundException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CurrentUserService {

    private final AppUserRepository appUserRepository;

    public AuthenticatedUser getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedUser user)) {
            throw new BadCredentialsException("Authentication is required");
        }

        return user;
    }

    public Long getCurrentUserId() {
        return getCurrentUser().id();
    }

    public AppUser getCurrentAppUser() {
        return appUserRepository.findById(getCurrentUserId())
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}
