package com.yjyh.phoneloan.backend.auth;

import com.yjyh.phoneloan.backend.auth.AuthDtos.AuthResponse;
import com.yjyh.phoneloan.backend.auth.AuthDtos.LoginRequest;
import com.yjyh.phoneloan.backend.auth.AuthDtos.RefreshRequest;
import com.yjyh.phoneloan.backend.auth.AuthDtos.RegisterRequest;
import com.yjyh.phoneloan.backend.auth.AuthDtos.UserResponse;
import com.yjyh.phoneloan.backend.common.CurrentUser;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class AuthController {
    private final AuthService authService;
    private final CurrentUser currentUser;

    public AuthController(AuthService authService, CurrentUser currentUser) {
        this.authService = authService;
        this.currentUser = currentUser;
    }

    @PostMapping("/auth/register")
    AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/auth/login")
    AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/auth/refresh")
    AuthResponse refresh(@Valid @RequestBody RefreshRequest request) {
        return authService.refresh(request);
    }

    @GetMapping("/me")
    UserResponse me(@RequestHeader("Authorization") String authorization) {
        return authService.toUserResponse(currentUser.fromAuthorization(authorization));
    }
}
