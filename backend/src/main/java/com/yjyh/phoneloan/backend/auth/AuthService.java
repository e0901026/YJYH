package com.yjyh.phoneloan.backend.auth;

import com.yjyh.phoneloan.backend.auth.AuthDtos.AuthResponse;
import com.yjyh.phoneloan.backend.auth.AuthDtos.LoginRequest;
import com.yjyh.phoneloan.backend.auth.AuthDtos.RefreshRequest;
import com.yjyh.phoneloan.backend.auth.AuthDtos.RegisterRequest;
import com.yjyh.phoneloan.backend.auth.AuthDtos.UserResponse;
import com.yjyh.phoneloan.backend.common.ApiException;
import com.yjyh.phoneloan.backend.invite.InviteCode;
import com.yjyh.phoneloan.backend.invite.InviteCodeRepository;
import com.yjyh.phoneloan.backend.user.User;
import com.yjyh.phoneloan.backend.user.UserRepository;
import com.yjyh.phoneloan.backend.user.UserRole;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final InviteCodeRepository inviteCodeRepository;
    private final AuthTokenRepository tokenRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthService(UserRepository userRepository, InviteCodeRepository inviteCodeRepository, AuthTokenRepository tokenRepository) {
        this.userRepository = userRepository;
        this.inviteCodeRepository = inviteCodeRepository;
        this.tokenRepository = tokenRepository;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        Instant now = Instant.now();
        if (userRepository.existsByEmployeeNo(request.employeeNo())) {
            throw new ApiException(HttpStatus.CONFLICT, "EMPLOYEE_NO_EXISTS", "工号已被注册");
        }
        InviteCode inviteCode = inviteCodeRepository.findByCode(request.inviteCode())
            .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "INVITE_CODE_INVALID", "邀请码无效"));
        if (!inviteCode.canUse(now)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVITE_CODE_INVALID", "邀请码无效或已过期");
        }
        User user = new User(
            UUID.randomUUID(),
            request.employeeNo(),
            request.name(),
            passwordEncoder.encode(request.password()),
            UserRole.USER,
            inviteCode.getCreatedByUserId(),
            now
        );
        userRepository.save(user);
        inviteCode.markUsed(user.getId(), now);
        return issueTokens(user, now);
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmployeeNo(request.employeeNo())
            .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "AUTH_FAILED", "工号或密码错误"));
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "AUTH_FAILED", "工号或密码错误");
        }
        return issueTokens(user, Instant.now());
    }

    @Transactional
    public AuthResponse refresh(RefreshRequest request) {
        AuthToken oldToken = tokenRepository.findByRefreshToken(request.refreshToken())
            .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "TOKEN_INVALID", "登录已失效"));
        User user = userRepository.findById(oldToken.getUserId())
            .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "TOKEN_INVALID", "登录已失效"));
        return issueTokens(user, Instant.now());
    }

    public User requireUser(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "AUTH_REQUIRED", "需要登录");
        }
        String token = authorization.substring("Bearer ".length());
        AuthToken authToken = tokenRepository.findByAccessToken(token)
            .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "TOKEN_INVALID", "登录已失效"));
        if (authToken.getExpiresAt().isBefore(Instant.now())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "TOKEN_INVALID", "登录已失效");
        }
        return userRepository.findById(authToken.getUserId())
            .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "TOKEN_INVALID", "登录已失效"));
    }

    public UserResponse toUserResponse(User user) {
        return new UserResponse(
            user.getId().toString(),
            user.getEmployeeNo(),
            user.getName(),
            user.getRole().name(),
            user.getInviteQuotaUsed()
        );
    }

    private AuthResponse issueTokens(User user, Instant now) {
        AuthToken token = new AuthToken(
            UUID.randomUUID(),
            user.getId(),
            UUID.randomUUID().toString(),
            UUID.randomUUID().toString(),
            now.plus(8, ChronoUnit.HOURS),
            now
        );
        tokenRepository.save(token);
        return new AuthResponse(token.getAccessToken(), token.getRefreshToken(), toUserResponse(user));
    }
}
