package com.yjyh.phoneloan.backend.user;

import com.yjyh.phoneloan.backend.common.ApiException;
import com.yjyh.phoneloan.backend.user.UserDtos.OwnerCreateUserRequest;
import com.yjyh.phoneloan.backend.user.UserDtos.OwnerUpdateUserRequest;
import com.yjyh.phoneloan.backend.user.UserDtos.OwnerUserResponse;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<OwnerUserResponse> ownerUsers(User user) {
        requireOwner(user);
        return userRepository.findAll().stream()
            .map(this::toOwnerUserResponse)
            .toList();
    }

    @Transactional
    public OwnerUserResponse create(User actor, OwnerCreateUserRequest request) {
        requireOwner(actor);
        if (blank(request.employeeNo()) || blank(request.name()) || blank(request.password())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "USER_REQUIRED_FIELD_MISSING", "请填写工号、名称和初始密码");
        }
        if (userRepository.existsByEmployeeNo(request.employeeNo())) {
            throw new ApiException(HttpStatus.CONFLICT, "EMPLOYEE_NO_EXISTS", "工号已存在");
        }
        Instant now = Instant.now();
        User user = new User(
            UUID.randomUUID(),
            request.employeeNo(),
            request.name(),
            passwordEncoder.encode(request.password()),
            parseRole(request.role()),
            actor.getId(),
            now
        );
        return toOwnerUserResponse(userRepository.save(user));
    }

    @Transactional
    public OwnerUserResponse update(User actor, UUID id, OwnerUpdateUserRequest request) {
        requireOwner(actor);
        User user = requireUser(id);
        if (blank(request.name())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "USER_REQUIRED_FIELD_MISSING", "请填写名称");
        }
        Instant now = Instant.now();
        user.updateProfile(request.name(), parseRole(request.role()), now);
        if (!blank(request.password())) {
            user.updatePasswordHash(passwordEncoder.encode(request.password()), now);
        }
        return toOwnerUserResponse(user);
    }

    @Transactional
    public OwnerUserResponse disable(User actor, UUID id) {
        requireOwner(actor);
        if (actor.getId().equals(id)) {
            throw new ApiException(HttpStatus.CONFLICT, "CANNOT_DISABLE_SELF", "不能停用当前登录账号");
        }
        User user = requireUser(id);
        user.disable(Instant.now());
        return toOwnerUserResponse(user);
    }

    private OwnerUserResponse toOwnerUserResponse(User row) {
        return new OwnerUserResponse(
            row.getId().toString(),
            row.getEmployeeNo(),
            row.getName(),
            row.getRole().name(),
            row.getInvitedByUserId() == null ? null : row.getInvitedByUserId().toString(),
            row.isEnabled(),
            row.getCreatedAt().toString()
        );
    }

    private User requireUser(UUID id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "用户不存在"));
    }

    private void requireOwner(User user) {
        if (user.getRole() != UserRole.OWNER) {
            throw new ApiException(HttpStatus.FORBIDDEN, "OWNER_REQUIRED", "需要 Owner 权限");
        }
    }

    private UserRole parseRole(String role) {
        return "OWNER".equalsIgnoreCase(role) ? UserRole.OWNER : UserRole.USER;
    }

    private boolean blank(String value) {
        return value == null || value.isBlank();
    }
}
