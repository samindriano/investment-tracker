package com.sam.finance.sahamlog.auth.dto;

public record AuthResponse(
    String accessToken,
    String tokenType,
    long expiresInSeconds,
    UserSummary user) {
}
