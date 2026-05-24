package com.yjyh.phoneloan.backend.user;

import com.yjyh.phoneloan.backend.common.CurrentUser;
import com.yjyh.phoneloan.backend.user.UserDtos.OwnerUserResponse;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/owner/users")
public class UserController {
    private final UserService userService;
    private final CurrentUser currentUser;

    public UserController(UserService userService, CurrentUser currentUser) {
        this.userService = userService;
        this.currentUser = currentUser;
    }

    @GetMapping
    List<OwnerUserResponse> ownerUsers(@RequestHeader("Authorization") String authorization) {
        return userService.ownerUsers(currentUser.fromAuthorization(authorization));
    }
}
