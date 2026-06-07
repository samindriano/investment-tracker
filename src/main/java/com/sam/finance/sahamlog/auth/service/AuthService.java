package com.sam.finance.sahamlog.auth.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sam.finance.sahamlog.auth.domain.AppUser;
import com.sam.finance.sahamlog.auth.dto.AuthRequest;
import com.sam.finance.sahamlog.auth.dto.AuthResponse;
import com.sam.finance.sahamlog.auth.dto.UserSummary;
import com.sam.finance.sahamlog.auth.repository.AppUserRepository;
import com.sam.finance.sahamlog.shared.exception.ConflictException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @Transactional
    public AuthResponse register(AuthRequest request) {
        String normalizedEmail = normalizeEmail(request.email());
        if (appUserRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new ConflictException("Email is already registered");
        }

        AppUser user = new AppUser();
        user.setEmail(normalizedEmail);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        AppUser savedUser = appUserRepository.save(user);

        AuthenticatedUser authenticatedUser = new AuthenticatedUser(
            savedUser.getId(),
            savedUser.getEmail(),
            savedUser.getPasswordHash());

        return buildAuthResponse(authenticatedUser);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(AuthRequest request) {
        String normalizedEmail = normalizeEmail(request.email());
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(normalizedEmail, request.password()));

        AuthenticatedUser authenticatedUser = appUserRepository.findByEmailIgnoreCase(normalizedEmail)
            .map(user -> new AuthenticatedUser(user.getId(), user.getEmail(), user.getPasswordHash()))
            .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        return buildAuthResponse(authenticatedUser);
    }

    private AuthResponse buildAuthResponse(AuthenticatedUser user) {
        return new AuthResponse(
            jwtService.generateToken(user),
            "Bearer",
            jwtService.getExpirationSeconds(),
            new UserSummary(user.id(), user.email()));
    }

    private String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase();
    }
}
