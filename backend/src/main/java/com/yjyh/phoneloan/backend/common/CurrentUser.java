package com.yjyh.phoneloan.backend.common;

import com.yjyh.phoneloan.backend.auth.AuthService;
import com.yjyh.phoneloan.backend.user.User;
import org.springframework.stereotype.Component;

@Component
public class CurrentUser {
    private final AuthService authService;

    public CurrentUser(AuthService authService) {
        this.authService = authService;
    }

    public User fromAuthorization(String authorization) {
        return authService.requireUser(authorization);
    }
}
