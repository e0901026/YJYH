package com.yjyh.phoneloan.backend.auth;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "auth_tokens")
public class AuthToken {
    @Id
    private UUID id;
    private UUID userId;
    private String accessToken;
    private String refreshToken;
    private Instant expiresAt;
    private Instant createdAt;

    protected AuthToken() {
    }

    public AuthToken(UUID id, UUID userId, String accessToken, String refreshToken, Instant expiresAt, Instant now) {
        this.id = id;
        this.userId = userId;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.expiresAt = expiresAt;
        this.createdAt = now;
    }

    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public String getAccessToken() { return accessToken; }
    public String getRefreshToken() { return refreshToken; }
    public Instant getExpiresAt() { return expiresAt; }
    public Instant getCreatedAt() { return createdAt; }
}
