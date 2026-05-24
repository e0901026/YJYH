package com.yjyh.phoneloan.backend.auth;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthTokenRepository extends JpaRepository<AuthToken, UUID> {
    Optional<AuthToken> findByAccessToken(String accessToken);
    Optional<AuthToken> findByRefreshToken(String refreshToken);
}
