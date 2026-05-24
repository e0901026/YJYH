package com.yjyh.phoneloan.backend.auth;

import jakarta.validation.constraints.NotBlank;

public final class AuthDtos {
    private AuthDtos() {
    }

    public record RegisterRequest(
        @NotBlank String employeeNo,
        @NotBlank String name,
        @NotBlank String password,
        @NotBlank String inviteCode
    ) {}

    public record LoginRequest(@NotBlank String employeeNo, @NotBlank String password) {}
    public record RefreshRequest(@NotBlank String refreshToken) {}
    public record AuthResponse(String accessToken, String refreshToken, UserResponse user) {}
    public record UserResponse(String id, String employeeNo, String name, String role, int inviteQuotaUsed) {}
}
