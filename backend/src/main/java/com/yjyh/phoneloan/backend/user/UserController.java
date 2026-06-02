package com.yjyh.phoneloan.backend.user;

import com.yjyh.phoneloan.backend.common.CurrentUser;
import com.yjyh.phoneloan.backend.user.UserDtos.OwnerCreateUserRequest;
import com.yjyh.phoneloan.backend.user.UserDtos.OwnerUpdateUserRequest;
import com.yjyh.phoneloan.backend.user.UserDtos.OwnerUserResponse;
import java.util.UUID;
import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestBody;
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

    @PostMapping
    OwnerUserResponse create(@RequestHeader("Authorization") String authorization, @RequestBody OwnerCreateUserRequest request) {
        return userService.create(currentUser.fromAuthorization(authorization), request);
    }

    @PutMapping("/{id}")
    OwnerUserResponse update(@RequestHeader("Authorization") String authorization, @PathVariable UUID id, @RequestBody OwnerUpdateUserRequest request) {
        return userService.update(currentUser.fromAuthorization(authorization), id, request);
    }

    @DeleteMapping("/{id}")
    OwnerUserResponse disable(@RequestHeader("Authorization") String authorization, @PathVariable UUID id) {
        return userService.disable(currentUser.fromAuthorization(authorization), id);
    }
}
